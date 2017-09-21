/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import nl.strohalm.cyclos.annotations.Inject;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.entities.sms.SmsLog;
import nl.strohalm.cyclos.entities.sms.SmsLogQuery;
import nl.strohalm.cyclos.entities.sms.SmsLogStatus;
import nl.strohalm.cyclos.entities.sms.SmsLogType;
import nl.strohalm.cyclos.services.elements.ElementService;
import nl.strohalm.cyclos.services.settings.SettingsService;
import nl.strohalm.cyclos.services.sms.SmsLogService;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.RelationshipHelper;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.query.QueryParameters;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue
 */
@Controller
public class SearchSmsLogsController extends BaseRestController {

    private DataBinder<SmsLogQuery> dataBinder;
    private SmsLogService smsLogService;
    private ElementService elementService;
    private SettingsService settingsService;
    private RequestHelper requestHelper;

    public void setDataBinder(DataBinder<SmsLogQuery> dataBinder) {
        this.dataBinder = dataBinder;
    }

    public void setElementService(ElementService elementService) {
        this.elementService = elementService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Inject
    public void setRequestHelper(RequestHelper requestHelper) {
        this.requestHelper = requestHelper;
    }

//    @Override
//    public void onLocalSettingsUpdate(final LocalSettingsEvent event) {
//        super.onLocalSettingsUpdate(event);
//        dataBinder = null;
//    }
    @Inject
    public void setSmsLogService(final SmsLogService smsLogService) {
        this.smsLogService = smsLogService;
    }

    public static class SearchSmsLogsRequest {

        private long memberId;
        private String status;
        private String type;
        private String begin;
        private String end;
        private List<SmsLog> smsLogs;

        public List<SmsLog> getSmsLogs() {
            return smsLogs;
        }

        public void setSmsLogs(List<SmsLog> smsLogs) {
            this.smsLogs = smsLogs;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }

        public String getBegin() {
            return begin;
        }

        public void setBegin(String begin) {
            this.begin = begin;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

    }

    public static class SearchSmsLogsResponse extends GenericResponse {

        private boolean mySmsLogs;
        private Long memberId;
        private QueryParameters queryParameters;
        private List<SmsLogStatus> statusList=new ArrayList<SmsLogStatus>();
        private List<SmsLogType> typesListnew= new ArrayList<SmsLogType>();

        public boolean isMySmsLogs() {
            
            return mySmsLogs;
        }

        public void setMySmsLogs(boolean mySmsLogs) {
            this.mySmsLogs = mySmsLogs;
        }

        public Long getMemberId() {
            return memberId;
        }

        public void setMemberId(Long memberId) {
            this.memberId = memberId;
        }

        public QueryParameters getQueryParameters() {
            return queryParameters;
        }

        public void setQueryParameters(QueryParameters queryParameters) {
            this.queryParameters = queryParameters;
        }

        public List<SmsLogStatus> getStatusList() {
            return statusList;
        }

        public void setStatusList(List<SmsLogStatus> statusList) {
            this.statusList = statusList;
        }

        public List<SmsLogType> getTypesListnew() {
            return typesListnew;
        }

        public void setTypesListnew(List<SmsLogType> typesListnew) {
            this.typesListnew = typesListnew;
        }

       

    }
    
    
    public static class SearchSMSLogsActionResponse extends GenericResponse{
        
       private List<SmsLog> smsLogs =new ArrayList<SmsLog>();

        public List<SmsLog> getSmsLogs() {
            return smsLogs;
        }

        public void setSmsLogs(List<SmsLog> smsLogs) {
            this.smsLogs = smsLogs;
        }
       
        
    }
    

    @RequestMapping(value = "member/searchSmsLogs", method = RequestMethod.POST)
    @ResponseBody
    public SearchSMSLogsActionResponse executeQuery(@RequestBody SearchSmsLogsRequest request) {
        SearchSMSLogsActionResponse response = new SearchSMSLogsActionResponse();
        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
        final SmsLogQuery query = new SmsLogQuery();
        query.setResultType(QueryParameters.ResultType.LIST);
        
        if(request.getStatus()!=null && !request.getStatus().equals("ALL"))
        query.setStatus(SmsLogStatus.valueOf(request.getStatus()));
         if(request.getType()!=null && !request.getType().equals("ALL"))
        query.setType(SmsLogType.valueOf(request.getType()));
        Member member= elementService.load(LoggedUser.member().getId(), RelationshipHelper.nested(Element.Relationships.GROUP, MemberGroup.Relationships.SMS_MESSAGES));
        System.out.println(member);
        query.setMember(member);
        
        Period period=new Period();
        Calendar cal=Calendar.getInstance();
        try{
        if(request.getBegin()!=null && !request.getBegin().equalsIgnoreCase("")){
            cal.setTime(sdf.parse(request.getBegin()));
            period.setBegin(cal);
        }
        if(request.getEnd()!=null && !request.getEnd().equalsIgnoreCase("")){
            cal.setTime(sdf.parse(request.getEnd()));
            period.setEnd(cal);
        }
        }catch(Exception ex){
         ex.printStackTrace();
        }
        query.setPeriod(period);
        
        final List<SmsLog> smsLogs = smsLogService.search(query);

        response.setSmsLogs(smsLogs);

        response.setStatus(0);
        return response;
    }

    @RequestMapping(value = "member/searchSmsLogs", method = RequestMethod.GET)
    @ResponseBody
    public SearchSmsLogsResponse prepareForm() {
        SearchSmsLogsResponse response = new SearchSmsLogsResponse();
        // Resolve member id
       
        long memberId = LoggedUser.user().getId();
        if (memberId < 1) {
            memberId = LoggedUser.element().getId();
        }
        final boolean mySmsLogs = memberId == LoggedUser.element().getId();

        // Load member
        final Member member = elementService.load(memberId, RelationshipHelper.nested(Element.Relationships.GROUP, MemberGroup.Relationships.SMS_MESSAGES));

         response.setMemberId(memberId);
         response.setMySmsLogs(mySmsLogs);
         
         response.getStatusList().addAll(Arrays.asList(SmsLogStatus.values()));
         response.getTypesListnew().addAll(Arrays.asList(SmsLogType.values()));

        response.setMySmsLogs(mySmsLogs);
//        RequestHelper.storeEnum(request, SmsLogStatus.class, "statusList");
//        RequestHelper.storeEnum(request, SmsLogType.class, "typesList");

       // return getDataBinder().readFromString(form.Query());
       return response;

    }

    private DataBinder<SmsLogQuery> getDataBinder() {
        if (dataBinder == null) {
            final LocalSettings settings = settingsService.getLocalSettings();
            final BeanBinder<SmsLogQuery> binder = BeanBinder.instance(SmsLogQuery.class);
            binder.registerBinder("period", DataBinderHelper.periodBinder(settings, "period"));
            binder.registerBinder("member", PropertyBinder.instance(Member.class, "member"));
            binder.registerBinder("type", PropertyBinder.instance(SmsLogType.class, "type"));
            binder.registerBinder("status", PropertyBinder.instance(SmsLogStatus.class, "status"));
            binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
            dataBinder = binder;
        }
        return dataBinder;
    }

}
