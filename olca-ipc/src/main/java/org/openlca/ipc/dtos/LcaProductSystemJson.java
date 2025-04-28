package org.openlca.ipc.dtos;

import org.openlca.core.matrix.linking.ProviderLinking;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

import java.security.Provider;

public class LcaProductSystemJson {
	public Long id;
	public String refId;
	public String name;
	public Long category;
	public ModelType type = ModelType.PRODUCT_SYSTEM;
	public Long process;
	public ProviderLinking providerLinking = ProviderLinking.PREFER_DEFAULTS;
	public ProcessType processType = ProcessType.LCI_RESULT;

	public ProductSystemDescriptor toDescriptor() {
		ProductSystemDescriptor psd = new ProductSystemDescriptor();
		psd.id = id;
		psd.refId = refId;
		psd.name = name;
		if (category != null) psd.category = category;

		return psd;
	}

	public static LcaProductSystemJson from(ProductSystem productSystem) {
		LcaProductSystemJson ps = new LcaProductSystemJson();
		ps.id = productSystem.id;
		ps.refId = productSystem.refId;
		ps.name = productSystem.name;
		if (productSystem.category != null) ps.category = productSystem.category.id;
		ps.type = ModelType.PRODUCT_SYSTEM;
		ps.process = productSystem.referenceProcess.id;
		return ps;
	}
}
