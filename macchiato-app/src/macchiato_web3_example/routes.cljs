(ns macchiato-web3-example.routes
  (:require
   [bidi.bidi :as bidi]
   [macchiato.util.response :as r]
   [cljs.nodejs :as node]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth])
  (:require-macros
   [hiccups.core :refer [html]]))

;; (enable-console-print!)

(def Web3 (node/require "web3"))
(def web3 (web3/create-web3 Web3 "http://localhost:8545/"))

(defn- clj->json [m]
  (->> m
       clj->js
       (.stringify js/JSON)))

(defn- eth-accounts []
  (web3-eth/accounts web3))

(defn- account-balance [address]
  (web3/from-wei Web3
                 (web3-eth/get-balance web3 address)
                 :ether))

(defn- accounts [req res raise]
  (-> {:accounts (eth-accounts)}
      clj->json
      (r/ok)
      (r/content-type "application/json")
      (res)))

(defn- balance [req res raise]
  (-> {:balance
       {:eth (-> (eth-accounts) first account-balance)}}
      clj->json
      (r/ok)
      (r/content-type "application/json")
      (res)))

(defn not-found [req res raise]
  (-> (r/not-found)
      (r/content-type "application/json")
      (res)))

(def routes
  ["/" {"accounts" accounts
        "balance" balance}])

(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))
