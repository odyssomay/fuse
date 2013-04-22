(ns fuse.util
  (:import org.eclipse.jgit.util.FileUtils
           org.eclipse.jgit.lib.ProgressMonitor))

;;;;
;;;; Print

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
(defn info    [& args] (printc RESET  args))
(defn warning [& args] (printc YELLOW args))
(defn error   [& args] (printc RED    args))
(defn success [& args] (printc GREEN  args))

;;;;
;;;; Misc

(defn delete-directory [f]
  (when (.exists f)
    (FileUtils/delete f FileUtils/RECURSIVE)))

;;;; JGit progress indicator

(defn repeat-char [n c]
  (apply str (take n (repeat c))))

(defn progress-printer [title-prefix progress-prefix]
  (let [width 40
        total (atom 0)
        finished (atom 0)
        running-task? (atom false)]
    (add-watch
      finished nil
      (fn [_ _ _ finished]
        (let [done (let [t @total]
                     (if (zero? t)
                       0
                       (min width 
                            (int (* width (/ finished t))))))]
          (print
            (str "\r" progress-prefix "["
                 (repeat-char done "=")
                 (repeat-char (- width done) "-")
                 "]"))
          (flush))))
    (reify ProgressMonitor
      (beginTask [this title total-work]
        (println (str (if @running-task? "\n")
                      title-prefix title))
        (reset! total total-work)
        (reset! finished 0)
        (reset! running-task? true))
      (endTask [this]
        (print "\n")
        (reset! running-task? false))
      (isCancelled [this] false)
      (start [this total-tasks])
      (update [this completed]
        (swap! finished + completed)))))
