package nl.strohalm.cyclos.webservices.rest.members.pending;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.stereotype.Controller;

import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.controls.members.pending.ValidateRegistrationForm;
import nl.strohalm.cyclos.controls.restapi.BaseRestController;
import nl.strohalm.cyclos.entities.customization.files.CustomizedFile.Type;
import nl.strohalm.cyclos.entities.exceptions.EntityNotFoundException;
import nl.strohalm.cyclos.entities.groups.GroupFilter;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.PendingMember;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.elements.exceptions.RegistrationAgreementNotAcceptedException;
import nl.strohalm.cyclos.services.groups.GroupFilterService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.utils.ActionHelper;
import nl.strohalm.cyclos.utils.CustomizationHelper;
import nl.strohalm.cyclos.utils.CustomizationHelper.CustomizationData;
import nl.strohalm.cyclos.utils.MessageHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.validation.ValidationException;
@Controller
public class ValidateRegistrationController extends BaseRestController{
	  private GroupFilterService  groupFilterService;
	    private CustomizationHelper customizationHelper;
	    private MessageHelper messageHelper;
	    private ElementService elementService;
	    private ActionHelper actionHelper;
	    public final MessageHelper getMessageHelper() {
			return messageHelper;
		}

		public final void setMessageHelper(MessageHelper messageHelper) {
			this.messageHelper = messageHelper;
		}

		public final ElementService getElementService() {
			return elementService;
		}

		public final void setElementService(ElementService elementService) {
			this.elementService = elementService;
		}

		public final ActionHelper getActionHelper() {
			return actionHelper;
		}

		public final void setActionHelper(ActionHelper actionHelper) {
			this.actionHelper = actionHelper;
		}

		public final SettingsService getSettingsService() {
			return settingsService;
		}

		public final void setSettingsService(SettingsService settingsService) {
			this.settingsService = settingsService;
		}

		public final GroupFilterService getGroupFilterService() {
			return groupFilterService;
		}

		public final CustomizationHelper getCustomizationHelper() {
			return customizationHelper;
		}

		private SettingsService settingsService;

	    @Inject
	    public void setCustomizationHelper(final CustomizationHelper customizationHelper) {
	        this.customizationHelper = customizationHelper;
	    }

	    @Inject
	    public void setGroupFilterService(final GroupFilterService groupFilterService) {
	        this.groupFilterService = groupFilterService;
	    }
	    public static class ValidateRegistrationRequestDTO{
	    	
	    	
	    }
	    
	    public static class ValidateRegistrationResponseDTO{
	    	
	    }

	    @Override
	    protected ActionForward executeAction(final ActionMapping mapping, final ActionForm actionForm, final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	        // The only interesting session attribute for us is inContainerPage. All others will be cleared out
	        final HttpSession session = request.getSession(false);
	        @SuppressWarnings("unchecked")
	        final Enumeration<String> attributeNames = session.getAttributeNames();
	        final ArrayList<String> attributesToBeRemoved = new ArrayList<String>();
	        // in order to prevent ConcurrentModificationException, first store the attributes to be removed
	        while (attributeNames.hasMoreElements()) {
	            final String name = attributeNames.nextElement();
	            if (!"inContainerPage".equals(name)) {
	                attributesToBeRemoved.add(name);
	            }
	        }
	        // and after that delete them
	        for (final String name : attributesToBeRemoved) {
	            session.removeAttribute(name);
	        }

	        final String validationKeyMessage = messageHelper.message("pendingMember.validationKey");
	        final ValidateRegistrationForm form = (ValidateRegistrationForm) actionForm;
	        final String key = form.getKey();

	        // Load the pending member, in order to check the container url
	        PendingMember pendingMember;
	        try {
	            pendingMember = elementService.loadPendingMemberByKey(key, PendingMember.Relationships.MEMBER_GROUP);
	        } catch (final EntityNotFoundException e) {
	            // The key is invalid
	            return ActionHelper.sendError(mapping, request, response, "errors.invalid", validationKeyMessage);
	        }

	        // Check for the container url
	        final String containerUrl = findContainerUrl(pendingMember);
	        if (containerUrl != null) {
	            final boolean inContainerPage = Boolean.TRUE.equals(session.getAttribute("inContainerPage"));
	            if (!inContainerPage) {
	                session.setAttribute("inContainerPage", true);
	                session.setAttribute("instantRedirectTo", request.getContextPath() + "/do" + mapping.getPath() + "?key=" + key);
	                return new ActionForward(containerUrl, true);
	            }
	        }

	        // Check for a non-empty key
	        if (StringUtils.isEmpty(key)) {
	            return ActionHelper.sendError(mapping, request, response, "errors.required", validationKeyMessage);
	        }

	        // Process the validation
	        try {
	            Member member;
	            try {
	                member = elementService.publicValidateRegistration(key);
	            } catch (final EntityNotFoundException e) {
	                // The key is invalid
	                return ActionHelper.sendError(mapping, request, response, "errors.invalid", validationKeyMessage);
	            } catch (final RegistrationAgreementNotAcceptedException e) {
	                // He should accept the agreement first
	                return ActionHelper.redirectWithParam(request, mapping.findForward("acceptAgreement"), "key", key);
	            }

	            // Set the correct session attribute for customized login page
	            String loginParamName = null;
	            Object loginParamValue = null;
	            final MemberGroup group = member.getMemberGroup();
	            if (StringUtils.isNotEmpty(group.getLoginPageName())) {
	                loginParamName = "groupId";
	                loginParamValue = group.getId();
	            } else {
	                // Try by group filter
	                for (final GroupFilter filter : group.getGroupFilters()) {
	                    if (filter.getLoginPageName() != null) {
	                        loginParamName = "groupFilterId";
	                        loginParamValue = filter.getId();
	                        break;
	                    }
	                }
	            }
	            if (loginParamName != null) {
	                session.setAttribute("loginParamName", loginParamName);
	                session.setAttribute("loginParamValue", loginParamValue);
	            }

	            // We will send the flow to the error page not to showing an error, but the created message
	            final boolean passwordGenerated = member.getMemberUser().isPasswordGenerated();
	            String messageKey;
	            if (!member.isActive()) {
	                messageKey = "createMember.public.awaitingActivation";
	            } else if (passwordGenerated) {
	                messageKey = "createMember.public.awaitingPassword";
	            } else {
	                messageKey = "createMember.public.validated";
	            }
	            ActionHelper.sendError(mapping, request, response, messageKey, member.getUsername());
	            return mapping.findForward("confirmation");
	        } catch (final ValidationException e) {
	            return ActionHelper.handleValidationException(mapping, request, response, e);
	        } catch (final Exception e) {
	            //actionHelper.generateLog(request, getServlet().getServletContext(), e);
	            return ActionHelper.sendError(mapping, request, response, null);
	        }
	    }

	    private String findContainerUrl(final PendingMember pendingMember) {
	        return LoggedUser.runAsSystem(new Callable<String>() {
	            @Override
	            public String call() throws Exception {
	                final MemberGroup group = pendingMember.getMemberGroup();
	                final CustomizationData customization = customizationHelper.findCustomizationOf(Type.STATIC_FILE, group, null, "login.jsp");
	                switch (customization.getLevel()) {
	                    case GROUP:
	                        return group.getContainerUrl();
	                    case GROUP_FILTER:
	                        final GroupFilter groupFilter = groupFilterService.load(customization.getId());
	                        return groupFilter.getContainerUrl();
					default:
						break;
	                }
	                // Get the container url if not have from group / group filter already
	                final LocalSettings localSettings = settingsService.getLocalSettings();
	                return localSettings.getContainerUrl();
	            }
	        });
	    }
	}



