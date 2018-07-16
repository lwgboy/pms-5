package com.bizvisionsoft.pms.filecabinet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.WorkPackage;

public class AddDocuToPackageACT {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(final @MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		WorkPackage wp = (WorkPackage) context.getInput();
		Selector.create("输出文件选择器", context, wp).open(l -> {
			if (l != null && l.size() > 0) {
				List<ObjectId> docuIds = new ArrayList<ObjectId>();
				l.forEach((Object docu) -> {
					docuIds.add(((Docu) docu).get_id());
				});
				ServicesLoader.get(DocumentService.class)
						.updateDocument(
								new FilterAndUpdate()
										.filter(new Document("_id", new Document("$in", docuIds))).update(
												new Document("$addToSet",
														new Document("workPackage_id",
																new Document("$each", Arrays.asList(wp.get_id())))))
										.bson());

				GridPart viewr = (GridPart) context.getContent();
				viewr.setViewerInput();
			}
		});
	}
}
