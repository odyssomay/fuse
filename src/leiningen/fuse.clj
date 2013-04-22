(ns leiningen.fuse
  (:use [fuse.core])
  (:require (fuse [core :as c]
                  [util :as u]))
  (:require [clojure.java.io :as jio]
            [leiningen.core.user :as lein-user]))

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
    uninstall        Remove clojurec installation.
    upgrade          Upgrade clojurec to latest version (git).
   
   Testing subtasks:
    test             Simple test of the clojurec compiler.
    clojure-test     Full unit testing of clojure in clojurec."
  {:arglists '([project]
               [project subtask])}
  [project & args]
  (let [subtask (if (empty? args)
                  "once" (first args))]
    (c/run-subtask project subtask)))
