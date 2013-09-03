(ns core-async-fun.async
  (:use [clojure.core.async]))

(defn perms
  ([input] (let [c (chan 1)] (go (<! (perms input "" c)) (close! c)) c))
  ([input prefix c]
    (go
      (if (empty? input)
        (>! c prefix)
        (doseq [chosen input]
          (let [nprefix  (str prefix chosen)
                ninput   (apply str (filter #(not= chosen %) input))]
            (<! (perms ninput nprefix c))))))))

(defn channel-to-seq
  [c]
  (lazy-seq
    (when-let [x (<!! c)]
      (cons x (channel-to-seq c)))))

(defn -main
  [& args]
  (let [pchan (perms "abcdefghijklmnopqrstuvwxyzABCDEF")]
    (doseq [x (take 100 (channel-to-seq pchan))]
      (println x))))

(defn main-old
  "Run the async fun"
  [& args]
  (println "Start some simple async fun")
  (let [c1 (chan)
        _  (go (Thread/sleep 2000) (>! c1 "quack")
        (Thread/sleep 1000) (>! c1 "queaka")
        (Thread/sleep 3000) (close! c1))
        ]
    (loop [x (<!! c1)]
      (when-not (nil? x)
        (println x)
        (recur (<!! c1)))))
  (println "End the fun"))
