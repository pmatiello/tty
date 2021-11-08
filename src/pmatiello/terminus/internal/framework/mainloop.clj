(ns pmatiello.terminus.internal.framework.mainloop)

(defn- render-output! [render-fn output! old-state new-state]
  (-> (atom []) (render-fn old-state new-state) output!))

(defn with-mainloop
  [handle-fn render-fn state input output!]
  (try
    (add-watch state
               ::state-changed
               (fn [_ _ old-state new-state]
                 (render-output! render-fn output! old-state new-state)))

    (swap! state assoc ::init true)
    (mapv handle-fn input)
    (finally
      (remove-watch state ::state-changed))))
