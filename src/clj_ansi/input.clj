(ns clj-ansi.input
  (:require [clj-ansi.internal.input :as internal.input])
  (:import (java.io Reader)))

(defn ^:private each->key [acc key]
  (cond
    (and (:has-next? key) (= (:char-code key) 27))
    (let [result @acc]
      (vreset! acc [key])
      result)

    (:has-next? key)
    (do (vswap! acc conj key)
        nil)

    :else
    (let [result (conj @acc key)]
      (vreset! acc [])
      result)))

(defn ^:private input-seq->key-seq [input-seq]
  (let [acc (volatile! [])]
    (->> input-seq
         (map (partial each->key acc))
         (remove empty?))))

(def ^:private key->key-codes
  (partial map :char-code))

(defn input-seq->char-seq [input-seq]
  (->> input-seq
       input-seq->key-seq
       (map key->key-codes)
       (map internal.input/key-codes->char)
       (remove #{::internal.input/omit})))

(defn reader->input-seq [^Reader reader]
  (lazy-seq
    (let [char-code (.read reader)
          _         (if (= 27 char-code)
                      (Thread/sleep 10))
          has-next? (.ready reader)]
      (cons {:char-code char-code :has-next? has-next?}
            (reader->input-seq reader)))))
