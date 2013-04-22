(ns leiningen.fuse
  (:use [fuse.core])
  (:require (fuse [core :as c]
                  [util :as u]))
  (:require [clojure.java.io :as jio]
            [leiningen.core.user :as lein-user]))

;;;;
;;;; Directories

(defn create-fuse-dir [env]
  (let [f (:install-path env)]
    (when-not (.exists f)
      (u/info "Creating fuse directory" (.getCanonicalPath f))
      (.mkdir f))))

(defn create-fuse-target-dir [env]
  (let [f (:target-path env)]
    (when-not (.exists f)
      (u/info "Creating fuse target directory"
              (.getCanonicalPath f))
      (.mkdir f))))

(defn create-directories [env]
  (create-fuse-dir env)
  (create-fuse-target-dir env))

;;;;
;;;; Env

(defn project->env [project]
  (let [env (:fuse project)
        fuse-dir (or (:install-path env)
                     (jio/file (lein-user/leiningen-home)
                               "fuse"))
        cljc-dir (jio/file fuse-dir "clojurec")
        target-path (or (:target-path env)
                        (jio/file (:target-path project) "fuse"))
        env (merge {:gcc-command "gcc"}
                   env
                   {:install-path (jio/file fuse-dir)
                    :target-path (jio/file target-path)
                    :cljc-path (jio/file cljc-dir)})]
    env))

;;;;
;;;; Task

(defn fuse
  "Compile clojure code to native C.
  
  Options:
   :src-dirs
   :compile?
  
  Calling without any subtask is the same as calling the subtask 'once'.
  
  Several subtasks are available:
   Compiling subtasks:
    auto             Compile once and recompile on file changes.
    clean            Removes all generated and compiled files.
    once             Compile once and exit.
   
   Installation subtasks:
    install          Install clojurec (does nothing if already installed).
    reinstall        Same as remove + install.
    uninstall        Remove clojurec installation.
    upgrade          Upgrade clojurec to latest version (git).
   
   Testing subtasks:
    test             Simple test of the clojurec compiler.
    clojure-test     Full unit testing of clojure in clojurec."
  {:arglists '([project]
               [project subtask])}
  [project & args]
  (let [subtask (if (empty? args) "once" (first args))
        f (case subtask
            "auto"  c/auto
            "clean" c/clean
            "once"  c/once
            
            "install"   c/install
            "reinstall" c/reinstall
            "uninstall" c/uninstall
            "upgrade"   c/upgrade
            
            "test"         c/test-run
            "clojure-test" c/clojure-test-run
            
            (do (u/error (str "fuse does not have a subtask called '" subtask "'"))
              (u/info "\nSee 'lein help fuse' for available subtasks.")))
        env (project->env project)]
    (when (c/check-for-gcc env)
      (create-directories env)
      (c/install-if-not-installed env)
      (f env))))
