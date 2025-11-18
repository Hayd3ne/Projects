package com.trader.network;

public interface TradeListener extends ConnectionListener {
    void onIncomingTrade(TradeProposal proposal);
    void onTradeAccepted(String tradeId);
}