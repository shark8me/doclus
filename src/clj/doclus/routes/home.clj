(ns doclus.routes.home
  (:require [doclus.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer  [redirect file-response]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io])
  (:import  [java.io File FileInputStream FileOutputStream])
  )

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(def resource-path "/tmp/")

(defn file-path  [path &  [filename]]
    (java.net.URLDecoder/decode
          (str path File/separator filename)
              "utf-8"))

(defn upload-file
  "uploads a file to the target folder
  when :create-path? flag is set to true then the target path will be created"
 [path  {:keys  [tempfile size filename]}]
   (try
     (with-open  [in  (new FileInputStream tempfile)
                  out  (new FileOutputStream  (file-path path filename))]
      (let  [source  (.getChannel in)
             dest   (.getChannel out)]
       (.transferFrom dest source 0  (.size source)) (.flush out)))))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))
  (GET "/upload"  [] (layout/render "upload.html"))
  (GET "/quil"  [] (layout/render "quil.html"))

  (POST "/upload"  [file]
    (upload-file resource-path file)
     (redirect  (str "/files/"  (:filename file))))
  (GET "/files/:filename"  [filename]
               (file-response  (str resource-path filename)))
)

