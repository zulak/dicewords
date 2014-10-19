(ns dicewords.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [dicewords.wordlist :refer [words]])
  (:import java.security.SecureRandom)
  (:gen-class))

(defn get-password [num-parts]
  (let [rand (java.security.SecureRandom/getInstance "SHA1PRNG")]
    (->>
     (range num-parts)
     (map (fn [& args] (nth words (.nextInt rand (count words)))))
     (string/join " "))))

(defn get-passwords [num-passwords password-length]
  (take num-passwords (repeatedly #(get-password password-length))))

(def cli-options
  [;; First three strings describe a short-option, long-option with optional
   ;; example argument description, and a description. All three are optional
   ;; and positional.
   ["-l" "--length LENGTH" "Password length"
    :default 6
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 2 % 32) "Must be a number between 2 and 32"]]
   ["-n" "--number NUMBER" "Number of passwords"
    :default 10
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 1 % 100) "Must be a number between 1 and 100"]]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Generate DiceWare passwords."
        ""
        "Usage: dicewords [options]"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main
  "Print generated passwords to the terminal and exit."
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
     (:help options) (exit 0 (usage summary))
     errors (exit 1 (string/join \newline errors)))
    (println (->>
              (get-passwords (:number options) (:length options))
              (string/join \newline)))))
