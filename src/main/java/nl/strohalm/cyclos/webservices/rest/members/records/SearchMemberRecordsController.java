/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members.records;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import nl.strohalm.cyclos.entities.customization.fields.MemberRecordCustomField;
import nl.strohalm.cyclos.entities.customization.fields.MemberRecordCustomFieldValue;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.members.records.FullTextMemberRecordQuery;
import nl.strohalm.cyclos.entities.members.records.MemberRecord;
import nl.strohalm.cyclos.entities.members.records.MemberRecordType;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.BeanCollectionBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Lue Infoservices
 */
@Controller
public class SearchMemberRecordsController extends BaseRestController {

    private DataBinder<FullTextMemberRecordQuery> dataBinder;

    public DataBinder<FullTextMemberRecordQuery> getDataBinder() {
        if (dataBinder == null) {
            final LocalSettings localSettings = settingsService.getLocalSettings();
            dataBinder = memberRecordQueryDataBinder(localSettings);
        }
        return dataBinder;
    }

    public static BeanBinder<FullTextMemberRecordQuery> memberRecordQueryDataBinder(final LocalSettings localSettings) {
        final BeanBinder<MemberRecordCustomFieldValue> customValuesBinder = BeanBinder.instance(MemberRecordCustomFieldValue.class);
        customValuesBinder.registerBinder("field", PropertyBinder.instance(MemberRecordCustomField.class, "field"));
        customValuesBinder.registerBinder("value", PropertyBinder.instance(String.class, "value"));

        final BeanBinder<FullTextMemberRecordQuery> binder = BeanBinder.instance(FullTextMemberRecordQuery.class);
        binder.registerBinder("element", PropertyBinder.instance(Element.class, "element"));
        binder.registerBinder("broker", PropertyBinder.instance(Member.class, "broker"));
        binder.registerBinder("period", DataBinderHelper.periodBinder(localSettings, "period"));
        binder.registerBinder("keywords", PropertyBinder.instance(String.class, "keywords"));
        binder.registerBinder("customValues", BeanCollectionBinder.instance(customValuesBinder, "customValues"));
        binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());
        return binder;
    }

    public static class SearchMemberRecordParameters {

        private Long typeId;
        private Long element;
        private Long broker;
        private String begin;
        private String end;
        private String keyword;

        public Long getTypeId() {
            return typeId;
        }

        public void setTypeId(Long typeId) {
            this.typeId = typeId;
        }

        public Long getElement() {
            return element;
        }

        public void setElement(Long element) {
            this.element = element;
        }

        public Long getBroker() {
            return broker;
        }

        public void setBroker(Long broker) {
            this.broker = broker;
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

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

    }

    public static class SearchMemberRecordResponse extends GenericResponse {

        List<MemberRecordEntity> memberRecordList;

        public List<MemberRecordEntity> getMemberRecordList() {
            return memberRecordList;
        }

        public void setMemberRecordList(List<MemberRecordEntity> memberRecordList) {
            this.memberRecordList = memberRecordList;
        }

    }

    public static class MemberRecordEntity {

        private Long id;
        private String by;
        private Calendar date;
        private Calendar lastModified;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getBy() {
            return by;
        }

        public void setBy(String by) {
            this.by = by;
        }

        public Calendar getDate() {
            return date;
        }

        public void setDate(Calendar date) {
            this.date = date;
        }

        public Calendar getLastModified() {
            return lastModified;
        }

        public void setLastModified(Calendar lastModified) {
            this.lastModified = lastModified;
        }

    }

    @RequestMapping(value = "admin/searchMemberRecords", method = RequestMethod.POST)
    @ResponseBody
    public SearchMemberRecordResponse search(@RequestBody SearchMemberRecordParameters params) {
        SearchMemberRecordResponse response = new SearchMemberRecordResponse();
        LocalSettings settings = settingsService.getLocalSettings();
        FullTextMemberRecordQuery query = new FullTextMemberRecordQuery();
        if (params.getKeyword() != null) {
            query.setKeywords(params.getKeyword());
        }
        if (params.getBegin() != null && params.getEnd() != null) {
            Period period = new Period();
            period.setBegin(settings.getDateConverter().valueOf(params.getBegin()));
            period.setEnd(settings.getDateConverter().valueOf(params.getEnd()));
            query.setPeriod(period);
        }
        if (params.getElement() != null) {
            query.setElement(elementService.load(params.getElement(), Element.Relationships.GROUP));
        }
        if (params.getBroker() != null) {
            query.setBroker((Member) elementService.load(params.getBroker()));
        }
        query.setType(memberRecordTypeService.load(params.getTypeId()));
        query.fetch(MemberRecord.Relationships.TYPE);
        final List<MemberRecord> memberRecords = memberRecordService.fullTextSearch(query);
        System.out.println("-----" + memberRecords);
        List<MemberRecordEntity> memberRecordList = new ArrayList();
        for (MemberRecord record : memberRecords) {
            MemberRecordEntity entity = new MemberRecordEntity();
            entity.setId(record.getId());
            entity.setDate(record.getDate());
            entity.setLastModified(record.getLastModified());
            entity.setBy(record.getBy().getName());
            memberRecordList.add(entity);

        }
        response.setMemberRecordList(memberRecordList);
        response.setStatus(0);
        return response;

    }
}
