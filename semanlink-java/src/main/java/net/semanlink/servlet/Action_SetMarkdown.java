package net.semanlink.servlet;
import java.io.File;

import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLVocab; 
import net.semanlink.sljena.JenaUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * le truc, c qu'on veut mettre le statement dans le docsModel
 * (et donc utiliser un statement de la forme theMarkdownFile markdownOf [un doc ou un kw]
 */
public class Action_SetMarkdown extends Action_SetOrAddProperty {
//	public static final String CREATE = "create"; // TODO
	protected String getPropUri(HttpServletRequest request) {
		return SLVocab.SL_MARKDOWN_OF_PROPERTY;
	}
	
	/** the uri of the markdown file */
	protected String getSubjectUri(HttpServletRequest request) {
//		boolean isCreateAction = (request.getParameter(CREATE) != null);
		String uri = null;
//		if (isCreateAction) {
//			SLModel mod = SLServlet.getSLModel();
//			File dir = mod.goodDirToSaveAFile();
//			String f = getPropValue(request);
//			int k = f.lastIndexOf("/");
//			f = f.substring(k+1);
//			f += ".md";
//			File file = new File(dir, f);
//		} else {
			uri = request.getParameter("markdown");
//		}
		
		String errMess = JenaUtils.getUriViolations(uri, false);
		if (errMess != null) {
			throw new RuntimeException(errMess);
		}
		return uri;
	}

	/** the uri of the kw or doc */
	protected String getPropValue(HttpServletRequest request) {
		return request.getParameter("uri");
	}
	
	// le sujet est tjrs le markdown doc
	@Override protected boolean subjectIsKwNotDoc(HttpServletRequest request) {
		return false;
	}

}
