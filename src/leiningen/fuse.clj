(ns leiningen.fuse
  (:use [fuse.core])
  (:require [clojure.java.io :as jio]
            [leiningen.core.user :as lein-user]))

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
;;;; Env

(defn project->env [project]
  (let [env (:fuse project)
        fuse-dir (or (:fuse-dir env) (fuse-dir))
        cljc-dir (or (:cljc-dir env)
                     (jio/file fuse-dir "clojurec"))
        env (merge {:fuse-dir fuse-dir
                    :cljc-dir cljc-dir}
                   env)]
    env))


(defn fuse
  "Compile clojure code to native C.
  
  Options:
   :cljc-src
   :cljc-compile?
  
  Calling without any subtask is the same as calling the subtask 'once'.
  
  Several subtasks are available:
   Compiling subtasks:
    auto             Compile once and recompile on file changes.
    clean            Removes all generated and compiled files.
    once             Compile once and exit.
   
   clojurec installation subtasks:
    install          Install clojurec (does nothing if already installed).
    reinstall        Same as remove + install.
    uninstall        Remove clojurec installation.
    upgrade          Upgrade clojurec to latest version (git).
   
   Testing subtasks:
    test             Simple test of the clojurec compiler.
    clojure-test     Full unit testing of clojure in clojurec."
  [project & args])
