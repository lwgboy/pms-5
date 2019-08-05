package com.bizvisionsoft.pms.problem.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.CauseConsequence;
import com.bizvisionsoft.service.model.ClassifyProblem;
import com.bizvisionsoft.service.model.DetectionInd;
import com.bizvisionsoft.service.model.FreqInd;
import com.bizvisionsoft.service.model.IncidenceInd;
import com.bizvisionsoft.service.model.LostInd;
import com.bizvisionsoft.service.model.Organization;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.model.SeverityInd;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class EditProblem {

	@Inject
	private String actionType;
	
	@Inject
	private String editorName;

	@Inject
	private String render;

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.EVENT) Event e) {
		if ("create".equals(actionType)) {
			create(context);
		} else if ("creates".equals(actionType)) {
			creates(context);
		} else if ("edits".equals(actionType) || "edits".equals(e.text)) {
			Problem problem = (Problem) context.getRootInput();
			edits(context, problem);
		} else if ("edit".equals(actionType) || "edit".equals(e.text)) {
			Problem problem = (Problem) context.getRootInput();
			edit(context, problem, true);
		} else if ("read".equals(actionType) || "read".equals(e.text)) {
			Problem problem = (Problem) context.getRootInput();
			edit(context, problem, false);
		}
	}

	private void edit(IBruiContext context, Problem problem, boolean editable) {
		String editor = "问题编辑器（编辑）.editorassy";
		if(null != editorName) {
			editor = editorName;
		}
		Editor.create(editor, context, problem, false).setEditable(editable).ok((r, t) -> {
			r.remove("_id");
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", problem.get_id())).set(r).bson();
			long l = Services.get(ProblemService.class).updateProblems(fu, br.getDomain());
			if (l > 0) {
				AUtil.simpleCopy(t, problem);// 改写problem
				Check.instanceThen(context.getContent(), IQueryEnable.class, q -> q.doRefresh());
			}
		});
	}

	private void edits(IBruiContext context, Problem problem) {
		String editor = "tops/问题编辑器（整体创建）.editorassy";
		if(null != editorName) {
			editor = editorName;
		}
		Editor.create(editor, context, new Document(), false).ok((r, t) -> {
			saveDocument(t, problem.get_id());
		});
	}

	private void creates(IBruiContext context) {
		String editor = "tops/问题编辑器（整体创建）.editorassy";
		if(null != editorName) {
			editor = editorName;
		}
		Editor.create(editor, context, new Document(), false).ok((r, t) -> {
			saveDocument(t, null);
		});
	}

	private void saveDocument(Document t, ObjectId problem_id) {
		ProblemService service = Services.get(ProblemService.class);
		String domain = br.getDomain();
		String lang = RWT.getLocale().getLanguage();

		boolean inserted = false;
		if (problem_id == null) {
			problem_id = new ObjectId();
			inserted = true;
		}

		Problem problem = makeProblem(t, problem_id);
		List<Document> d1CFT = makeD1CFT(t, problem_id);
		Document d2Desc = makeD2ProblemDesc(problem_id, t);
		List<Document> d2ProblemPhotos = makeD2ProblemPhotos(t, problem_id);
		List<Document> problemActions = makeProblemActions(t, problem_id);
		List<CauseConsequence> causeConsequences = makeCauseConsequence(t, problem_id);
		Document d4Root = makeD4Root(t, problem_id);
		List<Document> d7Similars = makeD7Similar(t, problem_id);
		List<Document> d8Exps = makeD8Exp(t, problem_id);

		// TODO 缺少判断是否为空
		// TODO 缺少更新
		if (inserted) {
			service.insertProblem(problem, domain);
			Check.isAssigned(d1CFT, l -> service.insertD1Items(l, domain));
			Check.isAssigned(d2Desc, l -> service.updateD2ProblemDesc((Document) l, lang, domain));
			Check.isAssigned(d2ProblemPhotos, l -> service.insertD2ProblemPhotos(l, domain));
			Check.isAssigned(problemActions, l -> service.insertActions(l, domain));
			Check.isAssigned(causeConsequences, l -> service.insertCauseConsequences(l, domain));
			Check.isAssigned(d4Root, l -> service.insertD4RootCauseDesc((Document) l, lang, domain));
			Check.isAssigned(d7Similars, l -> service.insertD7Similars(l, domain));
			Check.isAssigned(d8Exps, l -> service.insertD8Experiences(l, domain));
		} else {
			service.updateProblems(new FilterAndUpdate().filter(new BasicDBObject("_id", problem_id))
					.set(BsonTools.getBasicDBObject(problem, "_id")).bson(), domain);
			Check.isAssigned(d1CFT, l -> service.updateD1Items(l, domain));
			Check.isAssigned(d2Desc, l -> service.updateD2ProblemDesc((Document) l, lang, domain));
			Check.isAssigned(d2ProblemPhotos, l -> service.updateD2ProblemPhotos(l, domain));
			Check.isAssigned(problemActions, l -> service.updateActions(l, domain));
			Check.isAssigned(causeConsequences, l -> service.updateCauseConsequences(l, domain));
			Check.isAssigned(d4Root,
					l -> service.updateD4RootCauseDesc(
							new FilterAndUpdate().filter(new BasicDBObject("_id", ((Document) l).getObjectId("_id")))
									.set(BsonTools.getBasicDBObject((Document) l, "_id")).bson(),
							lang, domain));
			Check.isAssigned(d7Similars, l -> service.updateD7Similars(l, domain));
			Check.isAssigned(d8Exps, l -> service.updateD8Exps(l, domain));
		}
	}

	@SuppressWarnings("unchecked")
	private List<Document> makeD7Similar(Document doc, ObjectId problem_id) {
		List<Document> result = new ArrayList<Document>();
		Object d7Similars = doc.get("d7Similar");
		if (d7Similars != null)
			((List<Document>) d7Similars).forEach(d7Similar -> {
				d7Similar.append("problem_id", problem_id);
				result.add(d7Similar);
			});
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private List<Document> makeD8Exp(Document doc, ObjectId problem_id) {
		List<Document> result = new ArrayList<Document>();
		Object d8Exps = doc.get("exp");
		if (d8Exps != null)
			((List<Document>) d8Exps).forEach(d8Exp -> {
				d8Exp.append("problem_id", problem_id);
				result.add(d8Exp);
			});
		return result;
	}

	private Document makeD4Root(Document doc, ObjectId problem_id) {
		Document d4Root = new Document();
		d4Root.append("rootCauseDesc", doc.get("rootCauseDesc"));
		d4Root.append("rootCauseAtt", doc.get("rootCauseAtt"));
		d4Root.append("escapePoint", doc.get("escapePoint"));
		d4Root.append("escapePointAtt", doc.get("escapePointAtt"));
		d4Root.append("evidenceFile", doc.get("evidenceFile"));
		d4Root.append("charger", doc.get("charger"));
		d4Root.append("charger_meta", doc.get("charger_meta"));
		d4Root.append("date", doc.get("date"));
		d4Root.append("_id", problem_id);
		return d4Root;
	}

	@SuppressWarnings("unchecked")
	private List<CauseConsequence> makeCauseConsequence(Document doc, ObjectId problem_id) {
		List<CauseConsequence> result = new ArrayList<CauseConsequence>();
		Object causeRelations = doc.get("causeRelation");
		if (causeRelations != null)
			((List<Document>) causeRelations).forEach(cr -> {
				CauseConsequence cc = new CauseConsequence().setType((String) cr.get("type"))
						.setSubject((String) cr.get("subject"));
				BsonTools.decodeDocument(cr, cc);
				cc.setProblem_id(problem_id);
				result.add(cc);
			});
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<Document> makeD1CFT(Document doc, ObjectId problem_id) {
		List<Document> result = new ArrayList<Document>();
		Object d1CFTs = doc.get("d1CFT");
		if (d1CFTs != null)
			((List<Document>) d1CFTs).forEach(d1CFT -> {
				String role = d1CFT.getString("role");
				if ("组长".equals(role))
					d1CFT.append("role", "0");
				else if ("设计".equals(role))
					d1CFT.append("role", "1");
				else if ("工艺".equals(role))
					d1CFT.append("role", "2");
				else if ("生产".equals(role))
					d1CFT.append("role", "3");
				else if ("质量".equals(role))
					d1CFT.append("role", "4");
				else if ("顾客代表".equals(role))
					d1CFT.append("role", "5");
				else if ("ERA".equals(role))
					d1CFT.append("role", "6");
				else if ("ICA".equals(role))
					d1CFT.append("role", "7");
				else if ("PCA".equals(role))
					d1CFT.append("role", "8");
				else if ("SPA".equals(role))
					d1CFT.append("role", "9");
				else if ("LRA".equals(role))
					d1CFT.append("role", "10");
				d1CFT.append("problem_id", problem_id);
				result.add(d1CFT);
			});
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<Document> makeProblemActions(Document doc, ObjectId problem_id) {
		List<Document> result = new ArrayList<Document>();
		String[] keys = new String[] { "ica", "pca", "spa" };
		for (String key : keys) {
			Object keyValue = doc.get(key);
			if (keyValue != null)
				((List<Document>) keyValue).forEach(value -> {
					Document verification = new Document();
					verification.append("title", value.get("verificationTitle"));
					value.remove("verificationTitle");

					verification.append("comment", value.get("verificationComment"));
					value.remove("verificationComment");

					verification.append("attachment", value.get("verificationAttachment"));
					value.remove("verificationAttachment");

					verification.append("user", value.get("verificationUser"));
					value.remove("verificationTitle");

					verification.append("user_meta", value.get("verificationUser_meta"));
					value.remove("verificationUser_meta");

					verification.append("date", value.get("verificationDate"));
					value.remove("verificationDate");

					value.append("verification", verification);

					value.append("stage", key);

					value.append("problem_id", problem_id);

					result.add(value);
				});
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<Document> makeD2ProblemPhotos(Document doc, ObjectId problem_id) {
		List<Document> result = new ArrayList<Document>();
		Object d2ProblemPhotos = doc.get("d2ProblemPhoto");
		if (d2ProblemPhotos != null)
			((List<Document>) d2ProblemPhotos).forEach(d2ProblemPhoto -> {
				d2ProblemPhoto.append("problem_id", problem_id);
				result.add(d2ProblemPhoto);
			});

		return result;
	}

	private Document makeD2ProblemDesc(ObjectId problem_id, Document doc) {
		Document d2Desc = new Document("_id", problem_id);
		d2Desc.append("what", doc.get("what"));
		d2Desc.append("when", doc.get("when"));
		d2Desc.append("where", doc.get("where"));
		d2Desc.append("who", doc.get("who"));
		d2Desc.append("why", doc.get("why"));
		d2Desc.append("how", doc.get("how"));
		d2Desc.append("howmany", doc.get("howmany"));
		return d2Desc;
	}

	@SuppressWarnings("unchecked")
	private Problem makeProblem(Document doc, ObjectId problem_id) {
		Problem problem = br.newInstance(Problem.class);
		Arrays.asList(problem.getClass().getDeclaredFields()).forEach(srcField -> {
			try {
				srcField.setAccessible(true);
				String fieldName = srcField.getName();
				if ("severityInd".equals(fieldName)) {
					Object value = doc.get(fieldName + "_meta");
					if (value != null) {
						SeverityInd si = new SeverityInd();
						BsonTools.decodeDocument((Document) value, si);
						srcField.set(problem, si);
					}
				} else if ("freqInd".equals(fieldName)) {
					Object value = doc.get(fieldName + "_meta");
					if (value != null) {
						FreqInd fi = new FreqInd();
						BsonTools.decodeDocument((Document) value, fi);
						srcField.set(problem, fi);
					}
				} else if ("detectionInd".equals(fieldName)) {
					Object value = doc.get(fieldName + "_meta");
					if (value != null) {
						DetectionInd di = new DetectionInd();
						BsonTools.decodeDocument((Document) value, di);
						srcField.set(problem, di);
					}
				} else if ("lostInd".equals(fieldName)) {
					Object value = doc.get(fieldName + "_meta");
					if (value != null) {
						LostInd li = new LostInd();
						BsonTools.decodeDocument((Document) value, li);
						srcField.set(problem, li);
					}
				} else if ("incidenceInd".equals(fieldName)) {
					Object value = doc.get(fieldName + "_meta");
					if (value != null) {
						IncidenceInd ii = new IncidenceInd();
						BsonTools.decodeDocument((Document) value, ii);
						srcField.set(problem, ii);
					}
				} else if ("classifyProblems".equals(fieldName)) {
					Object cps = doc.get(fieldName + "_meta");
					if (cps != null && cps instanceof List) {
						List<ClassifyProblem> classifyProblems = new ArrayList<ClassifyProblem>();
						((List<Document>) cps).forEach(dept -> {
							ClassifyProblem cp = new ClassifyProblem();
							BsonTools.decodeDocument(dept, cp);
							classifyProblems.add(cp);
						});
						srcField.set(problem, classifyProblems);
					}
				} else if ("depts_id".equals(fieldName)) {
					Object depts = doc.get("depts_meta");
					if (depts != null && depts instanceof List) {
						List<Organization> orgs = new ArrayList<Organization>();
						((List<Document>) depts).forEach(dept -> {
							Organization org = new Organization();
							BsonTools.decodeDocument(dept, org);
							orgs.add(org);
						});
						problem.setOrganizations(orgs);
					}
				} else if ("_id".equals(fieldName))
					srcField.set(problem, problem_id);
				else
					srcField.set(problem, doc.get(fieldName));
			} catch (Exception e) {
			}
		});
		problem.setCreationInfo(br.operationInfo());
		return problem;
	}

	private void create(IBruiContext context) {
		Problem problem = br.newInstance(Problem.class).setCreationInfo(br.operationInfo());
		String editor = "问题编辑器（创建）.editorassy";
		if(null != editorName) {
			editor = editorName;
		}
		Editor.create(editor, context, problem, false).ok((r, t) -> {
			ProblemService service = Services.get(ProblemService.class);
			t = service.insertProblem(t, br.getDomain());
			if (t != null) {
				if (MessageDialog.openQuestion(br.getCurrentShell(), "创建问题初始记录", "问题已经创建成功，是否立即开始解决问题？")) {
					BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", t.get_id()))
							.set(new BasicDBObject("status", "解决中")).bson();
					if (service.updateProblems(fu, br.getDomain()) > 0) {
						Layer.message("问题解决程序已启动");
						br.switchPage("问题解决-TOPS过程", t.get_id().toHexString());
					}
				}
			}
		});
	}

}
