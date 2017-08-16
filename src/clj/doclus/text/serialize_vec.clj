(ns doclus.text.serialize-vec
  (:require [dl4clj.core :refer :all]
            [dl4clj.models.embeddings.loader.word-vector-serializer :as wvs]
            [dl4clj.models.embeddings.wordvectors.word-vectors :refer (similarity words-nearest get-word-vector)]
            [clojure.java.jdbc :as jdbc]
            [clojure.core.matrix :as m]
            [clojure.core.matrix.linear :as lm]
            ))

;;serialize the word2vec to postgres

(def db-spec {:dbname "postgres"
              :dbtype "postgres"
              :user "postgres"
              :password "postgres"})

(def cnames (into [
                   [:id :int "PRIMARY KEY"]
                   [:name :text]]
                  (for [i (range 300)]
                    [(keyword (str "v" (str i))) :float])))

(def fruit-table-ddl3
  (jdbc/create-table-ddl :vectors2 cnames))
;CREATE INDEX ON vectors2 (name);
(try
  (jdbc/db-do-commands db-spec [fruit-table-ddl3])
  (catch Exception e
    (clojure.stacktrace/print-cause-trace (.getNextException e))))

;(jdbc/db-do-commands db-spec [ (jdbc/drop-table-ddl :vectors2)])
(time (def w3 (wvs/load-google-model "/home/kiran/Downloads/GoogleNews-vectors-negative300.bin.gz" true)))
(class w3)
(count (get-word-vector w3 "india"))
(def lut (.lookupTable w3))

(def vocab (.getVocab lut))

(def words (.words vocab))
(count words)

(defn insert-all-words
  []
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

(time 
 (insert-all-words))

;;map 340 secs
;;for 10k words, partition 1000
;;31 s
;;without pmap 33secs
;;with 2 pmaps: 68 secs

(defn row-vec-fn
  [row]
  {(:name row) (m/array (mapv row (mapv #(-> (str "v" %) keyword) (range 300))))})

(take 10 words)
(time 
 (def res (jdbc/query db-spec ["SELECT * FROM vectors2 WHERE name = ?" "Gloria_Rubac" ]
                      {:row-fn row-vec-fn :result-set-fn (partial reduce merge {})}
                      )))
(count res)
(time
 (def res (jdbc/query db-spec
                      (into ["SELECT * FROM vectors2 WHERE name in (?,?,?,?,?,?,?,?,?,?)"] (take 10 words))
                      {:row-fn row-vec-fn :result-set-fn (partial reduce merge {})})))
(time (mapv #(get-word-vector w3 %) (take 10 words)))
(defn cosine-sim
  [v1 v2]
  (/ (m/dot v1 v2) (* (lm/norm v1) (lm/norm v2))))

(cosine-sim [2 4 3 1 6] [3 5 1 2 5])

