(ns fuse.core
  (:require [clojure.java.io :as jio]
            [leiningen.core.user :as lein-user])
  (:import org.eclipse.jgit.util.FileUtils
           (org.eclipse.jgit.api
             CloneCommand
             Git
             SubmoduleInitCommand
             SubmoduleUpdateCommand)
           org.eclipse.jgit.storage.file.FileRepositoryBuilder))

;;;; Print utilities

(def RESET  "\u001B[0m")
(def BLACK  "\u001B[30m")
(def RED    "\u001B[31m")
(def GREEN  "\u001B[32m")
(def YELLOW "\u001B[33m")
(def BLUE   "\u001B[34m")
(def PURPLE "\u001B[35m")
(def CYAN   "\u001B[36m")
(def WHITE  "\u001B[37m")

(defn printc [color args]
  (println (str color (apply print-str args)
                RESET)))

(defn section [& args] (printc CYAN   args))
(defn info    [& args] (printc WHITE  args))
(defn warning [& args] (printc YELLOW args))
(defn error   [& args] (printc RED    args))
(defn success [& args] (printc GREEN  args))

;;;;
;;;; Directories

(defn fuse-dir []
  (jio/file (lein-user/leiningen-home) "fuse"))

(defn create-fuse-dir []
  (let [f (fuse-dir)]
    (when-not (.exists f)
      (info "Creating fuse directory" (.getCanonicalPath f))
      (.mkdir f))))

(defn cljc-dir [] (jio/file (fuse-dir) "clojurec"))
;;;;
;;;; Repository

(defn file->repo [f]
  (-> (FileRepositoryBuilder.)
      (.setWorkTree f)
      .readEnvironment .findGitDir .build))

(defn add-cljc-repo [env]
  (let [repo (file->repo (:cljc-dir env))
        git (Git. repo)
        env (assoc env
              :repo repo
              :git git)]
    (assoc env :repo repo :git git)))

;;;;
;;;; Setting up clojurec

(defn upgrade-clojurec [env]
  (info " 1. Upgrading clojurec")
  (.pull (:git env)))

(defn download-clojurec [env]
  (info " 1. Downloading clojurec")
  (let [target (:cljc-dir env)]
    (when (.exists target)
      (info "    ...already exists"
            "(see subtasks upgrade and reinstall)"))
    (when-not (.exists target)
      (doto (CloneCommand.)
        (.setURI "https://github.com/schani/clojurec.git")
        (.setDirectory target)
        (.call)))))

(defn setup-submodules [env]
  (info " 2. Setting up submodules")
  (let [git (:git env)]
    (info "    * init")
    (.submoduleInit git)
    (info "    * update")
    (.submoduleUpdate git)))

(defn test-clojurec [env]
  (info " 3. Testing clojurec"))

(defn test-clojurec-full [env]
  (info "Testing clojure functionality in clojurec"
        "(this will take a while)"))

(defn install-clojurec [env upgrade?]
  (let [env (merge {:fuse-dir (fuse-dir)
                    :cljc-dir (jio/file (fuse-dir) "clojurec")}
                   env)]
    (create-fuse-dir)
    (when-not upgrade?
      (section "Installing clojurec")
      (download-clojurec env))
    (let [env (add-cljc-repo env)]
      (when upgrade?
        (section "Upgrading clojurec")
        (upgrade-clojurec env))
      (setup-submodules env)
      (test-clojurec env))
    (success "Done installing clojurec")))

;;;;
;;;; Tasks

(defn clean [env]
  (let [f (fuse-dir)]
    (when (.exists f)
      (FileUtils/delete f FileUtils/RECURSIVE))))

(defn install [env] (install-clojurec env false))

(defn reinstall [env] (clean env) (install env))

(defn upgrade [env] (install-clojurec env true))

(defn test-run [env]
  (test-clojurec (-> env add-cljc-repo)))

(defn clojure-test-run [env]
  (test-clojurec-full (-> env add-cljc-repo)))
