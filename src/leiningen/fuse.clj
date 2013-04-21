(ns leiningen.fuse
  (:require [fuse.core :as fuse]))

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
   
   Clojurec installation subtasks:
    install          Install clojurec (does nothing if already installed).
    reinstall        Same as remove + install.
    uninstall        Remove clojurec installation.
    upgrade          Upgrade clojurec to latest version (git).
   
   Testing subtasks:
    test             Simple test of the clojurec compiler.
    clojure-test     Full unit testing of clojure in clojurec."
  [project & args])
