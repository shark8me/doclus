(ns doclus.text.serialize-vec
  (:require [dl4clj.core :refer :all]
            [dl4clj.models.embeddings.loader.word-vector-serializer :as wvs]
            [dl4clj.models.embeddings.wordvectors.word-vectors :refer (similarity words-nearest get-word-vector)]
            [clojure.java.jdbc :as jdbc]
            [clojure.core.matrix :as m]
            [clojure.core.matrix.linear :as lm]
            [clojure.java.io :as io]
            [think.tsne.core :refer :all]
            ))

;;serialize the word2vec to postgres
(def db-spec {:dbname "postgres"
              :dbtype "postgres"
              :user "postgres"
              :password "postgres"})

(defn create-table
  "create the table to store w2vec data "
  []
  (let [cnames (into [[:id :int "PRIMARY KEY"]
                      [:name :text]]
                     (for [i (range 300)]
                       [(keyword (str "v" (str i))) :float]))
        ct (jdbc/create-table-ddl :vectors2 cnames)]
    (jdbc/db-do-commands db-spec [ct])))

;;create index to decrease query time
;CREATE INDEX ON vectors2 (name);

(defn drop-table
  "drop w2vec table"
  []
  (jdbc/db-do-commands db-spec [ (jdbc/drop-table-ddl :vectors2)]))

(defn load-wordvector
  "returns the word-vector map"
  []
  (let [w3 (wvs/load-google-model "/home/kiran/Downloads/GoogleNews-vectors-negative300.bin.gz" true)]
    (.words (.getVocab (.lookupTable w3)))))

(defn insert-all-words
  [w3 words]
  (let [iseq (->> (pmap (fn[w i]
                          (let [imap {:name w :id i}
                                      wv (get-word-vector w3 w)
                                wvf (->> (map #(assoc {}
                                                       (-> (str "v" %2)
                                                                    keyword) %1) wv (iterate inc 0))
                                               (reduce merge))]
                                  (merge imap wvf)))
                        words (iterate inc 1))
                  (partition 1000)
                  (map vec))
        ires (doseq [i iseq ]
               (jdbc/insert-multi! db-spec :vectors2 i))]
    ires))

(comment 
  (time 
   (insert-all-words)))

;;map 340 secs
;;for 10k words, partition 1000
;;31 s
;;without pmap 33secs
;;with 2 pmaps: 68 secs

(defn row-vec-fn
  [row]
  {(:name row) (m/array (mapv row (mapv #(-> (str "v" %) keyword) (range 300))))})

;;(def words (load-wordvector))
(take 10 words)

(defn get-wordvector
  [word]
  (jdbc/query db-spec ["SELECT * FROM vectors2 WHERE name = ?"  word ]
              {:row-fn row-vec-fn :result-set-fn (partial reduce merge {})}))

;;(get-wordvector "Gloria_Rubac")


(defn get-wordvectors
  [words]
  (let [cw (count words)
        w (str "(" (clojure.string/join "," (repeat cw "?" )) ")")
        query (into [(str "SELECT * FROM vectors2 WHERE name in " w)] words)]
    (jdbc/query db-spec
                query 
                {:row-fn row-vec-fn :result-set-fn (partial reduce merge {})})))

(time 
 (get-wordvectors (take 10 words)))
(time (mapv #(get-word-vector w3 %) (take 10 words)))

(defn cosine-sim
  [v1 v2]
  (/ (m/dot v1 v2) (* (lm/norm v1) (lm/norm v2))))

;(cosine-sim [2 4 3 1 6] [3 5 1 2 5])

;;stopwords
(def stopwords 
  (set (.split (slurp (io/resource "docs/stopwords.txt")) "\n" )))
(stopwords "to")
(stopwords "in")

(defn get-words
  [docsrc]
  (->> (.split (slurp (io/resource docsrc)) "\n")
       (mapcat #(.split % " "))
       (map #(.toLowerCase %))
       (remove #(stopwords %)) 
       (map #(.replaceAll % "[^a-z]" ""))
       (remove empty?)
       set))

(def g (->> (mapv get-wordvectors
                  (partition 10 (get-words "docs/wirearticle.txt")))
            (reduce merge)))

(class g)
(keys g)
(-> g vals first class)
(class (vals g))
(def res (tsne (core-mat-to-double-doubles (vals g)) 2))
(-> res first second)
(-> res first count)
