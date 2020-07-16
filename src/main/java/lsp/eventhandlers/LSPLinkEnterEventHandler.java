package lsp.eventhandlers;

import org.matsim.core.events.handler.EventHandler;

import org.matsim.contrib.freight.controler.LSPFreightLinkEnterEvent;

public interface LSPLinkEnterEventHandler extends EventHandler{

	public void handleEvent(LSPFreightLinkEnterEvent event);

}
