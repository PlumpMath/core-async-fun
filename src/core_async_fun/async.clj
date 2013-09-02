(ns core-async-fun.async
  (:use [clojure.core.async]))

(defn perms
  [input]
  (let [c (chan 1)]
    (go
      (loop [pre ""
             in  input]
        (if (empty? in)
          (>! c pre)
          (doseq [chosen input]
            (let [npre   (str pre chosen)
                  ninput (apply str (filter #(not= chosen %) input))]
              (recur npre ninput)))))
      (close! c))
    c))


(defn -main
  [& args]
  (println (<!! (perms "abc"))))

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
