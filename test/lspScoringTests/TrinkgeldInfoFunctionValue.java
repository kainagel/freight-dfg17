package lspScoringTests;

import lsp.functions.InfoFunctionValue;

public class TrinkgeldInfoFunctionValue  implements InfoFunctionValue{

	private String name = "TRINKGELD IN EUR";
	private Class<?> type;
	private double value;
	
	public TrinkgeldInfoFunctionValue() {
		this.type = double.class;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getDataType() {
		return type;
	}

	@Override
	public String getValue() {
		return String.valueOf(value);
	}

	@Override
	public void setValue(String value) {
		this.value = Double.valueOf(value).doubleValue();
	}

}
