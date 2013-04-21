(ns fuse.plugin)

(defn middleware [project]
  (let [src-path (.getCanonicalPath (jio/file (:target-path project)
                                              "fuse" "src"))]
    (update-in project [:source-paths] conj src-path)))
