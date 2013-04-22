(ns fuse.install
  (:require (fuse [test :as test]
                  [util :as u]))
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
      (u/info "    * Removing previous installation")
      (u/delete-directory target))
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
    (doto
      (.submoduleUpdate git)
      (.setProgressMonitor
        (u/progress-printer "    * "
                            "      "))
      .call)))

(defn install-clojurec [env upgrade?]
  (when-not upgrade?
    (u/section "Installing clojurec")
    (download-clojurec env))
  (let [env (add-cljc-repo env)]
    (when upgrade?
      (u/section "Upgrading clojurec")
      (upgrade-clojurec env))
    (setup-submodules env)
    (u/info " 3. Testing clojurec")
    (test/test-clojurec env))
  (u/success "Done installing clojurec"))

(defn install [env] (install-clojurec env false))

(defn upgrade [env] (install-clojurec env true))
