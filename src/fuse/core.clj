(ns fuse.core
  (:require (fuse install
                  [util :as u])
            [clojure.java.shell :as jsh]))

;;;;
;;;; Tasks

;;;; Compiling

(defn auto [env]
  (u/error "Not implemented yet, sorry! :("))

(defn clean [env]
  (let [f (:target-path env)]
    (u/info "Removing" (.getCanonicalPath f))
    (u/delete-directory f)))

(defn once [env])

;;;; Installing

(defn install [env] (fuse.install/install env))

(defn uninstall [env]
  (let [f (:install-path env)]
    (u/delete-directory f)))

(defn upgrade [env] (fuse.install/upgrade env))

;;;; Testing

(defn test-run [env]
  ;(test/test-clojurec (-> env add-cljc-repo))
  )

(defn clojure-test-run [env]
  ;(test/test-clojurec-full (-> env add-cljc-repo))
  )

;;;; Help tasks

(defn install-if-not-installed [env])

(defn check-for-gcc [env]
  (try
    (jsh/sh (:gcc-command env) "--version")
    true
    (catch java.io.IOException e
      (u/error "Couldn't run gcc.")
      (println e)
      (u/info "\nPlease install gcc or specify a gcc"
              "command in the fuse options."
              "See 'lein help fuse' for more info.")
      nil)))

;;;;
;;;; Entry point


