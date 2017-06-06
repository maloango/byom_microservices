package nl.strohalm.cyclos.webservices.rest.loans;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts.action.ActionForward;

import nl.strohalm.cyclos.controls.ActionContext;
import nl.strohalm.cyclos.controls.loans.DiscardLoanForm;
import nl.strohalm.cyclos.entities.accounts.loans.Loan;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.transactions.LoanPaymentDTO;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;

public class DiscardLoanController extends BaseRestController {

	protected ActionForward handleSubmit(final ActionContext context)
			throws Exception {
		final DiscardLoanForm form = context.getForm();

		final LoanPaymentDTO dto = resolveLoanDTO(context);
		final Loan loan = dto.getLoan();
		if (shouldValidateTransactionPassword(context, loan)) {
			context.checkTransactionPassword(form.getTransactionPassword());
		}
		loanService.discard(dto);

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("loanId", form.getLoanId());
		params.put("memberId", form.getMemberId());
		params.put("loanGroupId", form.getLoanGroupId());
		context.sendMessage("loan.discarded");
		return ActionHelper.redirectWithParams(context.getRequest(),
				context.getSuccessForward(), params);
	}


	protected void initDataBinder(
			final BeanBinder<? extends LoanPaymentDTO> binder) {
		super.initDataBinder(binder);
		final LocalSettings localSettings = settingsService.getLocalSettings();
		binder.registerBinder(
				"date",
				PropertyBinder.instance(Calendar.class, "date",
						localSettings.getRawDateConverter()));
	}

}
