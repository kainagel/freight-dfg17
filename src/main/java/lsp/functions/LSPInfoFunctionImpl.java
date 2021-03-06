package lsp.functions;

import java.util.ArrayList;
import java.util.Collection;

class LSPInfoFunctionImpl implements LSPInfoFunction {

	private Collection<LSPInfoFunctionValue<?>> values;
	
	LSPInfoFunctionImpl() {
		this.values = new ArrayList<LSPInfoFunctionValue<?>>();
	}
	
	@Override
	public Collection<LSPInfoFunctionValue<?>> getValues() {
		return values;
	}

}
