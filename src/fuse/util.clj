(ns fuse.util)

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
(defn info    [& args] (printc WHITE  args))
(defn warning [& args] (printc YELLOW args))
(defn error   [& args] (printc RED    args))
(defn success [& args] (printc GREEN  args))

;;;; File

(defn delete-directory [f]
  (when (.exists f)
    (FileUtils/delete f FileUtils/RECURSIVE)))