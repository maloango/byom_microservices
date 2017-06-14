package nl.strohalm.cyclos.webservices.rest.accounts.pos;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.accounts.pos.SearchPosForm;
import nl.strohalm.cyclos.entities.accounts.pos.Pos;
import nl.strohalm.cyclos.entities.accounts.pos.PosQuery;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.services.accounts.pos.PosService;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

@Controller
public class SearchPosController extends BaseRestController {

	private DataBinder<PosQuery> dataBinder;

	private PosService posService;
	private ElementService elementService;

	// Used to get data and save to database
	public DataBinder<PosQuery> getDataBinder() {
		if (dataBinder == null) {

			final BeanBinder<PosQuery> binder = BeanBinder
					.instance(PosQuery.class);
			binder.registerBinder("posId",
					PropertyBinder.instance(String.class, "posId"));
			binder.registerBinder("statuses", SimpleCollectionBinder.instance(
					PosQuery.QueryStatus.class, "statuses"));
			binder.registerBinder("member",
					PropertyBinder.instance(Member.class, "member"));
			binder.registerBinder("pageParameters",
					DataBinderHelper.pageBinder());

			dataBinder = binder;
		}
		return dataBinder;
	}

	@Inject
	public void setPosService(final PosService posService) {
		this.posService = posService;
	}

	public static class SearchPosRequestDto {

	}

	public static class SearchPosResponseDto {
		private List<Pos> pos;

		public void setPos(List<Pos> pos) {
			this.pos = pos;
		}

	}

	@RequestMapping(value = "member/searchPos", method = RequestMethod.GET)
	@ResponseBody
	protected SearchPosResponseDto executeQuery(
			@RequestBody SearchPosRequestDto form,
			final QueryParameters queryParameters) {
		final PosQuery query = (PosQuery) queryParameters;
		final List<Pos> pos = posService.search(query);
		SearchPosResponseDto response = new SearchPosResponseDto();
		response.setPos(pos);
		return response;

	}

	protected QueryParameters prepareForm(final ActionContext context) {

		final SearchPosForm form = context.getForm();
		final HttpServletRequest request = context.getRequest();
		final PosQuery query = getDataBinder().readFromString(form.getQuery());

		// Members
		if (query.getMember() != null) {
			final Member member = elementService.load(
					query.getMember().getId(), Element.Relationships.USER);
			query.setMember(member);
		}
		if (context.isBroker()) {
			query.setBroker((Member) context.getElement());
		}
		RequestHelper
				.storeEnum(request, PosQuery.QueryStatus.class, "statuses");

		return query;
	}

	protected boolean willExecuteQuery(final ActionContext context,
			final QueryParameters queryParameters) throws Exception {
		return true;
	}
}
