(ns fuse.core
  (:require (fuse compile install
                  [util :as u])
            (clojure.java
              [io :as jio]
              [shell :as jsh])
            [leiningen.core.user :as lein-user]))

;;;;
;;;; Tasks

;;;; Compiling

(def auto fuse.compile/auto)

(defn clean [env]
  (u/delete-directory (:target-path env)))

(def once fuse.compile/once)

;;;; Installing

(def install fuse.install/install)

(defn uninstall [env]
  (u/delete-directory (:install-path env)))

(def upgrade fuse.install/upgrade)

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
;;;; Directories

(defn create-directories [env]
  (u/create-directory (:install-path env) "install")
  (u/create-directory (:cljc-path env))
  (u/create-directory (:target-path env) "target"))

;;;;
;;;; Env

(defn project->env [project]
  (let [env (:fuse project)
        fuse-dir (or (:install-path env)
                     (jio/file (lein-user/leiningen-home)
                               "fuse"))
        cljc-dir (jio/file fuse-dir "clojurec")
        target-path (jio/file (:target-path project)
                              (or (:target-path env)
                                  "fuse"))
        env (merge {:gcc-command "gcc"}
                   env
                   {:install-path (jio/file fuse-dir)
                    :target-path (jio/file target-path)
                    :cljc-path (jio/file cljc-dir)})]
    env))

;;;;
;;;; Entry point

(defn run-subtask [project subtask]
  (let [f (case subtask
            "auto"  auto
            "clean" clean
            "once"  once
            
            "install"   install
            "uninstall" uninstall
            "upgrade"   upgrade
            
            "test"         test-run
            "clojure-test" clojure-test-run
            
            (do (u/error (str "fuse does not have a subtask called '" subtask "'"))
                (u/info "\nSee 'lein help fuse' for available subtasks.")))
        env (project->env project)]
    (when (check-for-gcc env)
      (create-directories env)
      (install-if-not-installed env)
      (f env))))
