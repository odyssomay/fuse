(ns fuse.core
  (:require [clojure.java.shell :as jsh])
  (:import org.eclipse.jgit.util.FileUtils
           (org.eclipse.jgit.api
             CloneCommand
             Git)
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
  (info " 1. Upgrading clojurec")
  (.pull (:git env)))

(defn download-clojurec [env]
  (info " 1. Downloading clojurec")
  (let [target (:cljc-path env)]
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
  (when-not upgrade?
    (section "Installing clojurec")
    (download-clojurec env))
  (let [env (add-cljc-repo env)]
    (when upgrade?
      (section "Upgrading clojurec")
      (upgrade-clojurec env))
    (setup-submodules env)
    (test-clojurec env))
  (success "Done installing clojurec"))

;;;;
;;;; Tasks

;;;; Compiling

(defn auto [env])

(defn clean [env])

(defn once [env])

;;;; Installing

(defn install [env] (install-clojurec env false))

(defn uninstall [env]
  (let [f (:fuse-path env)]
    (when (.exists f)
      (FileUtils/delete f FileUtils/RECURSIVE))))

(defn reinstall [env] (uninstall env) (install env))

(defn upgrade [env] (install-clojurec env true))

;;;; Testing

(defn test-run [env]
  (test-clojurec (-> env add-cljc-repo)))

(defn clojure-test-run [env]
  (test-clojurec-full (-> env add-cljc-repo)))
(defn check-for-gcc [env]
  (try
    (jsh/sh (:gcc-command env) "--version")
    true
    (catch java.io.IOException e
      (error "Couldn't run gcc.")
      (println e)
      (info "\nPlease install gcc or specify a gcc"
            "command in the fuse options."
            "See 'lein help fuse' for more info.")
      nil)))
