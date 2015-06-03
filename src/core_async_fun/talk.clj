(ns core-async-fun.talk
  (:use [clojure.core.async])
  (:require [clojure.walk :as walk]))

;;;;;;;;;;;;;;;;

(defn print-perms
  [input prefix]
  (if (empty? input)
    (println prefix)
    (doseq [[index chosen] (map-indexed vector input)]
      (let [nprefix  (str prefix chosen)
            ninput   (str (.substring input 0 index) (.substring input (inc index)))]
        (print-perms ninput nprefix)))))

(print-perms "abcd" "")

;;;;;;;;;;;;;;;;

(defn standard-perms
  [input prefix]
  (if (empty? input)
    [prefix]
    (mapcat (fn [[index chosen]]
              (let [nprefix  (str prefix chosen)
                    ninput   (str (.substring input 0 index) (.substring input (inc index)))]
                (standard-perms ninput nprefix)))
            (map-indexed vector input))))

(standard-perms "abcd" "")

(take 10 (standard-perms "1234567890" ""))

(def super-string (apply str (range 700)))

super-string

(take 10 (standard-perms super-string ""))

;;;;;;;;;;;;;;;;

(defn channel-perms
  ([input] (let [c (chan 1)] (go (<! (channel-perms input "" c)) (close! c)) c))
  ([input prefix c]
    (go
      (if (empty? input)
        (>! c prefix)
        (doseq [[index chosen] (map-indexed vector input)]
          (let [nprefix  (str prefix chosen)
                ninput   (str (.substring input 0 index) (.substring input (inc index)))]
            (<! (channel-perms ninput nprefix c))))))))

(defn channel-to-seq
  [c]
  (lazy-seq
    (when-let [x (<!! c)]
      (cons x (channel-to-seq c)))))

(defn channel-perms-helper
  [input]
  (channel-to-seq (channel-perms input)))

(channel-perms-helper "abcd")

(take 10 (channel-perms-helper "1234567890"))

(take 10 (channel-perms-helper super-string))

;;;;;;;;;;;;;;;;

(defgen give-me-123
  []
  (yield 1)
  (yield 2)
  (yield 3))

'(1 2 3)

(defgen forever-123
  []
  (yield 1)
  (yield 2)
  (yield 3)
  (forever-123))

;;;;;;;;;;;;;;;;

(defn rewrite-if-yield
  [block channel-symbol]
  (if (and (list? block)
           (= 'yield (first block)))
    (cons '>! (cons channel-symbol (rest block)))
    block))

(defn rewrite-if-recursive
  [block fn-symbol replacement-fn-symbol]
  (if (and (list? block)
           (= fn-symbol (first block)))
    (list '<! (cons replacement-fn-symbol (rest block)))
    block))

(defmacro defgen
  [fn-name args & body]
  (let [body-fn-name (gensym)
        channel-name (gensym)
        replace-fn   #(-> %
                          (rewrite-if-yield channel-name)
                          (rewrite-if-recursive fn-name body-fn-name))
        new-body     (walk/postwalk replace-fn body)]
    `(defn ~fn-name
       ~args
       (let [~channel-name  (chan 1)
             body-fn#       (fn ~body-fn-name
                              ~args
                              (go
                                ~@new-body))]
         (go
           (<! (body-fn# ~@args))
           (close! ~channel-name))
         (channel-to-seq ~channel-name)))))

;;;;;;;;;;;;;;;;

(defgen give-me-123
  []
  (yield 1)
  (yield 2)
  (yield 3))

(give-me-123)

(defgen forever-123
  []
  (yield 1)
  (yield 2)
  (yield 3)
  (forever-123))

(take 30 (forever-123))

(nth (forever-123) 30000)

;;;;;;;;;;;;;;;;

(defgen gen-perms
  [input prefix]
  (if (empty? input)
    (yield prefix)
    (doseq [[index chosen] (map-indexed vector input)]
      (let [nprefix  (str prefix chosen)
            ninput   (str (.substring input 0 index) (.substring input (inc index)))]
        (gen-perms ninput nprefix)))))


(gen-perms "abcd" "")

(take 10 (gen-perms "1234567890" ""))

(take 10 (gen-perms super-string ""))

