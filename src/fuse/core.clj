(ns fuse.core
  (:require (fuse install
                  [util :as u])
            (clojure.java
              [io :as jio]
              [shell :as jsh])
            [leiningen.core.user :as lein-user]))

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

(defn install [env] (fuse.install/install env))

(defn uninstall [env]
  (let [f (:install-path env)]
    (if (.exists f)
      (u/info "Removing" (.getCanonicalPath f)))
    (u/delete-directory f)))

(defn upgrade [env] (fuse.install/upgrade env))

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

(defn create-fuse-dir [env]
  (let [f (:install-path env)]
    (when-not (.exists f)
      (u/info "Creating install directory" (.getCanonicalPath f))
      (.mkdir f))))

(defn create-fuse-target-dir [env]
  (let [f (:target-path env)]
    (when-not (.exists f)
      (u/info "Creating target directory"
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
