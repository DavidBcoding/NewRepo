(ns blank.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [blank.ajax :as ajax]
    [blank.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string]
    [ajax.core :refer [GET POST]])
  (:import goog.History))

(defn nav-link [uri title page]
      [:a.navbar-item
       {:href  uri
        :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
       title])

(defn navbar []
      (r/with-let [expanded? (r/atom false)]
                  [:nav.navbar.is-info>div.container
                   [:div.navbar-brand
                    [:a.navbar-item {:href "/" :style {:font-weight :bold}} "blank"]
                    [:span.navbar-burger.burger
                     {:data-target :nav-menu
                      :on-click    #(swap! expanded? not)
                      :class       (when @expanded? :is-active)}
                     [:span] [:span] [:span]]]
                   [:div#nav-menu.navbar-menu
                    {:class (when @expanded? :is-active)}
                    [:div.navbar-start
                     [nav-link "#/" "Home" :home]
                     [nav-link "#/about" "About" :about]]]]))

(defn about-page []
      [:section.section>div.container>div.content
       [:img {:src "/img/warning_clojure.png"}]])



;; Calls the math API for a specific operation and x and y values
(defn math [operation x y]
      (ajax.core/POST (str "/api/math/" operation)
                      {:headers       {"accept" "application/transit-json"}
                       :params        {:x x :y y}
                       :error-handler #(do
                                         (println %)
                                         (rf/dispatch [:total 0]))
                       :handler       #(do
                                         (println %)
                                         (rf/dispatch [:total %]))}))

;; Function for hard coded math API for Year + 1

(defn int-value [v]
      (-> v .-target .-value int))


(defn change-color [total]
      (cond
        (<= 0 (:total @total) 19) {:style {:color "lightgreen" :font-weight :bold}}
        (<= 20 (:total @total) 49) {:style {:color "lightblue" :font-weight :bold}}
        :default {:style {:color "lightsalmon" :font-weight :bold}}))


(rf/reg-sub
  :x
  (fn [db _]
      (:x db)))

(rf/reg-event-db
  :x
  (fn [db [_ new-value]]
      (assoc db :x new-value)))


(comment


  @re-frame.db/app-db

  @(rf/subscribe [:x])

  ())

(rf/reg-sub
  :y
  (fn [db _]
      (:y db)))

(rf/reg-event-db
  :y
  (fn [db [_ new-value]]
      (assoc db :y new-value)))


(comment

  @re-frame.db/app-db

  @(rf/subscribe [:y])

  ())

(rf/reg-sub
  :total
  (fn [db _]
      (:total db)))

(rf/reg-event-db
  :total
  (fn [db [_ new-value]]
      (assoc db :total (:result new-value))))

(defn home-page []
      (r/with-let [x (rf/subscribe [:x])
                   y (rf/subscribe [:y])
                   total (rf/subscribe [:total])]
                  [:div.content.box
                   [:section.section>div.container>div.content
                    [:p "Enter numbers in the text boxes below for your own equation then click an operation for your answer."]
                    [:form
                     [:div.form-group
                      [:input {:type :text :value (str @x) :placeholder "First number here" :on-change #(rf/dispatch [:x (int-value %)])}]]
                     [:p]
                     [:div.form-group
                      [:input {:type :text :value (str @y) :placeholder "Second number here" :on-change #(rf/dispatch [:y (int-value %)])}]]
                     [:p]
                     [:button.button.is-primary {:on-click #(math "plus" @x @y)} "+"]
                     [:button.button.is-black {:on-click #(math "minus" @x @y)} "-"]
                     [:button.button.is-primary {:on-click #(math "multiply" @x @y)} "x"]
                     [:button.button.is-black {:on-click #(math "divide" @x @y)} "/"]
                     [:p]
                     [:div.form-group
                      [:label "Your answer is: " + [:span (change-color total) @total]]]]]]))

(defn page []
      (if-let [page @(rf/subscribe [:common/page])]
              [:div
               [navbar]
               [page]]))

(defn navigate! [match _]
      (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name :home
           :view #'home-page}]
     ;       :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page}]]))

(defn start-router! []
      (rfe/start!
        router
        navigate!
        {}))

;; -------------------------
;; Initialize app
(defn ^:dev/after-load mount-components []
      (rf/clear-subscription-cache!)
      (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
      (start-router!)
      (ajax/load-interceptors!)
      (mount-components))


(comment
  (ajax.core/POST (str "/api/math/" "plus")
                  {:headers       {"accept" "application/transit-json"}
                   :params        {:x @(rf/subscribe [:x]) :y @(rf/subscribe [:y])}
                   :error-handler #(do
                                     (println %))
                   ; (rf/dispatch [:total 0]))
                   :handler       #(do
                                     (println %))})
  ; (rf/dispatch [:total %]))})
  (ajax.core/POST (str "/api/math/" "minus")
                  {:headers       {"accept" "application/transit-json"}
                   :params        {:x @(rf/subscribe [:x]) :y @(rf/subscribe [:y])}
                   :error-handler #(do
                                     (println %))
                   ; (rf/dispatch [:total 0]))
                   :handler       #(do
                                     (println %))})
  ; (rf/dispatch [:total %]))})
  (ajax.core/POST (str "/api/math/" "multiply")
                  {:headers       {"accept" "application/transit-json"}
                   :params        {:x @(rf/subscribe [:x]) :y @(rf/subscribe [:y])}
                   :error-handler #(do
                                     (println %))
                   ; (rf/dispatch [:total 0]))
                   :handler       #(do
                                     (println %))})
  ; (rf/dispatch [:total %]))})
  (ajax.core/POST (str "/api/math/" "divide")
                  {:headers       {"accept" "application/transit-json"}
                   :params        {:x @(rf/subscribe [:x]) :y @(rf/subscribe [:y])}
                   :error-handler #(do
                                     (println %))
                   ; (rf/dispatch [:total 0]))
                   :handler       #(do
                                     (println %))})
  ; (rf/dispatch [:total %]))})
  ())










