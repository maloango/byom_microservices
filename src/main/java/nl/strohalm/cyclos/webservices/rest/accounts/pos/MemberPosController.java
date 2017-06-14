package nl.strohalm.cyclos.webservices.rest.accounts.pos;

import java.util.Calendar;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.access.AdminMemberPermission;
import nl.strohalm.cyclos.access.BrokerPermission;
import nl.strohalm.cyclos.access.Permission;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.pos.EditPosForm;
import nl.strohalm.cyclos.entities.accounts.pos.MemberPos;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.exceptions.PermissionDeniedException;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.permissions.PermissionService;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class MemberPosController extends BaseRestController {

	private DataBinder<MemberPos> dataBinder;
	private ElementService elementService;
	private PermissionService permissionService;
	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	// Used to get data and save to database
	public DataBinder<MemberPos> getDataBinder() {
		if (dataBinder == null) {

			final BeanBinder<MemberPos> binder = BeanBinder
					.instance(MemberPos.class);
			binder.registerBinder("posId",
					PropertyBinder.instance(String.class, "posId"));
			binder.registerBinder("posName",
					PropertyBinder.instance(String.class, "posName"));
			binder.registerBinder("date",
					PropertyBinder.instance(Calendar.class, "date"));

			dataBinder = binder;
		}
		return dataBinder;
	}

	protected void prepareForm(final ActionContext context) throws Exception {

		final EditPosForm form = context.getForm();
		final HttpServletRequest request = context.getRequest();
		final long memberId = form.getMemberId();
		if (memberId <= 0) {
			throw new PermissionDeniedException();
		}
		final Member member = elementService.load(memberId);
		final Collection<MemberPos> memberPos = member.getPosDevices();
		request.setAttribute("memberPos", memberPos);
		request.setAttribute("memberId", memberId);
		request.setAttribute("isOwnUser", member.equals(context.getElement()));
		final Permission permission = context.isBroker() ? BrokerPermission.POS_ASSIGN
				: AdminMemberPermission.POS_ASSIGN;
		request.setAttribute("canAssign",
				permissionService.hasPermission(permission));

	}
}
