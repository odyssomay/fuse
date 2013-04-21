(ns leiningen.fuse
  (:use [fuse.core])
  (:require [clojure.java.io :as jio]
            [leiningen.core.user :as lein-user]))

;;;;
;;;; Directories

(defn create-fuse-dir [env]
  (let [f (:fuse-path env)]
    (when-not (.exists f)
      (info "Creating fuse directory" (.getCanonicalPath f))
      (.mkdir f))))

;;;;
;;;; Env

(defn project->env [project]
  (let [env (:fuse project)
        fuse-dir (or (:fuse-path env)
                     (jio/file (lein-user/leiningen-home)
                               "fuse"))
        cljc-dir (or (:cljc-path env)
                     (jio/file fuse-dir "clojurec"))
        env (merge {:fuse-path (jio/file fuse-dir)
                    :cljc-path (jio/file cljc-dir)
                    :gcc-command "gcc"}
                   env)]
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
            "auto"  auto
            "clean" clean
            "once"  once
            
            "install"   install
            "reinstall" reinstall
            "uninstall" uninstall
            "upgrade"   upgrade
            
            "test"         test-run
            "clojure-test" clojure-test-run
            
            (do (error (str "fuse does not have a subtask called '" subtask "'"))
              (info "\nSee 'lein help fuse' for available subtasks.")))
        env (project->env project)]
    (create-fuse-dir env)
    (install-if-not-installed env)
    (check-for-gcc env)
    (f env)))
