(ns fuse.core
  (:require (fuse [util :as u])
            [clojure.java.shell :as jsh])
  (:import (org.eclipse.jgit.api CloneCommand Git)
           org.eclipse.jgit.storage.file.FileRepositoryBuilder))

;;;;
;;;; Repository

(defn file->repo [f]
  (-> (FileRepositoryBuilder.)
      (.setWorkTree f)
      .readEnvironment .findGitDir .build))

(defn add-cljc-repo [env]
  (let [repo (file->repo (:cljc-path env))
        git (Git. repo)
        env (assoc env
              :repo repo
              :git git)]
    (assoc env :repo repo :git git)))

;;;;
;;;; Setting up clojurec

(defn upgrade-clojurec [env]
  (u/info " 1. Upgrading clojurec")
  (.call (.pull (:git env))))

(defn download-clojurec [env]
  (u/info " 1. Downloading clojurec")
  (let [target (:cljc-path env)]
    (when (.exists target)
      (u/info "    ...already exists"
              "(see subtasks upgrade and reinstall)"))
    (when-not (.exists target)
      (doto (CloneCommand.)
        (.setURI "https://github.com/schani/clojurec.git")
        (.setDirectory target)
        (.setProgressMonitor
          (u/progress-printer "    * "
                              "      "))
        (.call)))))

(defn setup-submodules [env]
  (u/info " 2. Setting up submodules")
  (let [git (:git env)]
    (u/info "    * init")
    (.call (.submoduleInit git))
    (u/info "    * update")
    (.call (.submoduleUpdate git))))

(defn test-clojurec [env]
  (u/info " 3. Testing clojurec"))

(defn test-clojurec-full [env]
  (u/info "Testing clojure functionality in clojurec"
          "(this will take a while)"))

(defn install-clojurec [env upgrade?]
  (when-not upgrade?
    (u/section "Installing clojurec")
    (download-clojurec env))
  (let [env (add-cljc-repo env)]
    (when upgrade?
      (u/section "Upgrading clojurec")
      (upgrade-clojurec env))
    (setup-submodules env)
    (test-clojurec env))
  (u/success "Done installing clojurec"))

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

(defn install [env] (install-clojurec env false))

(defn uninstall [env]
  (let [f (:install-path env)]
    (u/delete-directory f)))

(defn reinstall [env] (uninstall env) (install env))

(defn upgrade [env] (install-clojurec env true))

;;;; Testing

(defn test-run [env]
  (test-clojurec (-> env add-cljc-repo)))

(defn clojure-test-run [env]
  (test-clojurec-full (-> env add-cljc-repo)))

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
