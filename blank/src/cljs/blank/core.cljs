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
    [clojure.string :as string])
  (:import goog.History))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
   title])

(defn navbar [] 
  (r/with-let [expanded? (r/atom false)]
              [:nav.navbar.is-info>div.container
               [:div.navbar-brand
                [:a.navbar-item {:href "/" :style {:font-weight :bold}} "blank"]
                [:span.navbar-burger.burger
                 {:data-target :nav-menu
                  :on-click #(swap! expanded? not)
                  :class (when @expanded? :is-active)}
                 [:span][:span][:span]]]
               [:div#nav-menu.navbar-menu
                {:class (when @expanded? :is-active)}
                [:div.navbar-start
                 [nav-link "#/" "Home" :home]
                 [nav-link "#/about" "About" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn home-page []
      (def str1 "Hello")
      (r/with-let [x (rf/subscribe [:x])
                   y (rf/subscribe [:y])
                   total (rf/subscribe [:total])]
                  [:div.content.box
                   [:p "I hope everyone is having a great day!"]
                   [:button.button.is-primary {:on-click #(getAdd)} "2022 + 1"]
                   [:p "That answer is: "
                    [:span @total]]
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
                      [:label "Your answer is: " + [:span (change-color) @total]]]]]]))

(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div
     [navbar]
     [page]]))

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
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
