// /* Created on 2 oct. 06 */
//package net.semanlink.servlet;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//
//import net.semanlink.delicious.DeliciousSynchro;
//import net.semanlink.semanlink.SLModel;
//
//import org.apache.struts.action.ActionForm;
//import org.apache.struts.action.ActionForward;
//import org.apache.struts.action.ActionMapping;
//
//public class Action_Delicious extends BaseAction {
//		public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
//			ActionForward x = null;
//			try {
//		  	Jsp_Page jsp = new Jsp_Page(request);
//		  	String title;
//		  	title = "Delicious Synchronization";
//				request.setAttribute("net.semanlink.servlet.jsp", jsp);
//				jsp.setContent("/jsp/delicious.jsp");
//				jsp.setTitle(title);
//				
//				HttpSession session = request.getSession();
//				DeliciousSynchro deliciousSynchro = (DeliciousSynchro) session.getAttribute("net.semanlink.delicious.DeliciousSynchro");
//				
//				String user = request.getParameter("user");
//
//				// Form_Delicious deliciousForm = (Form_Delicious) form;
//				
//				if ( (deliciousSynchro == null) || (!(deliciousSynchro.getUser().equals(user)))) {
//					if (user != null) {
//						String password = request.getParameter("password");
//						SLModel mod = SLServlet.getSLModel();
//						try {
//							deliciousSynchro = new DeliciousSynchro(mod, user, password, 
//									SLServlet.getProxyHost(), SLServlet.getProxyPort(), SLServlet.getProxyUserName(),SLServlet.getProxyPassword());
//							deliciousSynchro.initImport();
//							session.setAttribute("net.semanlink.delicious.DeliciousSynchro", deliciousSynchro);
//						} catch (del.icio.us.DeliciousNotAuthorizedException e) {
//							e.printStackTrace();
//							request.setAttribute("errorMess" , "del.icio.us.DeliciousNotAuthorizedException");
//						}
//					}
//				}
//				
//				String what = request.getParameter("what");
//				if ("importbundles".equals(what)) {
//					if (deliciousSynchro != null) deliciousSynchro.importBundles(request, response, null);	
//				}
//				
//				x = mapping.findForward("continue");
//			} catch (Exception e) {
//				return error(mapping, request, e );
//			}
//			return x;
//		} // end execute
//} // end Action
