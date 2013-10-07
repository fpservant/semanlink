package net.semanlink.servlet;

import javax.servlet.http.*;
/**
 */
public class Action_TreeOrNot extends Action_Set {
String getParamName() {
	return "mode";
}

void toSessionAttribute(HttpServletRequest request) {
	param2SessionAttribute(getParamName(), request);
}
} // end Action
