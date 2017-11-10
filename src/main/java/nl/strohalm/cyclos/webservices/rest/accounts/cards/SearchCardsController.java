package nl.strohalm.cyclos.webservices.rest.accounts.cards;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.servlet.http.HttpServletRequest;
import nl.strohalm.cyclos.entities.accounts.cards.Card;
import nl.strohalm.cyclos.entities.accounts.cards.CardQuery;
import nl.strohalm.cyclos.entities.accounts.cards.CardType;
import nl.strohalm.cyclos.entities.groups.AdminGroup;
import nl.strohalm.cyclos.entities.groups.Group;
import nl.strohalm.cyclos.entities.groups.GroupFilterQuery;
import nl.strohalm.cyclos.entities.groups.GroupQuery;
import nl.strohalm.cyclos.entities.groups.MemberGroup;
import nl.strohalm.cyclos.entities.members.Element;
import nl.strohalm.cyclos.entities.members.Member;
import nl.strohalm.cyclos.entities.settings.LocalSettings;
import nl.strohalm.cyclos.utils.Period;
import nl.strohalm.cyclos.utils.RequestHelper;
import nl.strohalm.cyclos.utils.access.LoggedUser;
import nl.strohalm.cyclos.utils.binding.BeanBinder;
import nl.strohalm.cyclos.utils.binding.DataBinder;
import nl.strohalm.cyclos.utils.binding.DataBinderHelper;
import nl.strohalm.cyclos.utils.binding.PropertyBinder;
import nl.strohalm.cyclos.utils.binding.SimpleCollectionBinder;
import nl.strohalm.cyclos.webservices.rest.BaseRestController;
import nl.strohalm.cyclos.webservices.rest.GenericResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchCardsController extends BaseRestController {

    public static class SearchCardsResponse extends GenericResponse {

        private List<MemberGroupEntity> memberGroups;
        private List<Card.Status> cardStatus;
        private List<CardTypeEntity> cards;

        public List<CardTypeEntity> getCards() {
            return cards;
        }

        public void setCards(List<CardTypeEntity> cards) {
            this.cards = cards;
        }

        public List<Card.Status> getCardStatus() {
            return cardStatus;
        }

        public void setCardStatus(List<Card.Status> cardStatus) {
            this.cardStatus = cardStatus;
        }

        public List<MemberGroupEntity> getMemberGroups() {
            return memberGroups;
        }

        public void setMemberGroups(List<MemberGroupEntity> memberGroups) {
            this.memberGroups = memberGroups;
        }

    }

    public static class MemberGroupEntity {

        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public static class CardTypeEntity {

        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @RequestMapping(value = "admin/searchCards", method = RequestMethod.GET)
    @ResponseBody
    public SearchCardsResponse prepareForm() {
        SearchCardsResponse response = new SearchCardsResponse();
        //groups
        final GroupQuery groupQuery = new GroupQuery();
        final GroupFilterQuery groupFilterQuery = new GroupFilterQuery();
        final AdminGroup adminGroup = LoggedUser.group();
        groupFilterQuery.setAdminGroup(adminGroup);
        groupQuery.setNatures(Group.Nature.MEMBER, Group.Nature.BROKER);
        groupQuery.setManagedBy(adminGroup);
        final List<MemberGroup> groups = (List<MemberGroup>) groupService.search(groupQuery);
        List<MemberGroupEntity> memberGroups = new ArrayList();
        for (MemberGroup group : groups) {
            MemberGroupEntity entity = new MemberGroupEntity();
            entity.setId(group.getId());
            entity.setName(group.getName());
            memberGroups.add(entity);
        }
        response.setMemberGroups(memberGroups);
        // Card status

        List<Card.Status> cardStatus = new ArrayList();
        cardStatus.add(Card.Status.ACTIVE);
        cardStatus.add(Card.Status.BLOCKED);
        cardStatus.add(Card.Status.CANCELED);
        cardStatus.add(Card.Status.EXPIRED);
        cardStatus.add(Card.Status.PENDING);
        response.setCardStatus(cardStatus);

        // CardTypes
        final List<CardType> cardTypes = cardTypeService.listAll();
        List<CardTypeEntity> cards = new ArrayList();
        for (CardType type : cardTypes) {
            CardTypeEntity entity = new CardTypeEntity();
            entity.setId(type.getId());
            entity.setName(type.getName());
            cards.add(entity);
        }
        response.setCards(cards);
        response.setStatus(0);
        return response;

    }

    public static class CardsParameters {

        private String expiration_begin;
        private String expiration_end;
        private Long member;
        private BigInteger number;
        private List<String> cardStatus;
        private List<Long> groups;
        private Long cardType;

        public Long getCardType() {
            return cardType;
        }

        public void setCardType(Long cardType) {
            this.cardType = cardType;
        }

        public String getExpiration_begin() {
            return expiration_begin;
        }

        public void setExpiration_begin(String expiration_begin) {
            this.expiration_begin = expiration_begin;
        }

        public String getExpiration_end() {
            return expiration_end;
        }

        public void setExpiration_end(String expiration_end) {
            this.expiration_end = expiration_end;
        }

        public Long getMember() {
            return member;
        }

        public void setMember(Long member) {
            this.member = member;
        }

        public BigInteger getNumber() {
            return number;
        }

        public void setNumber(BigInteger number) {
            this.number = number;
        }

        public List<String> getCardStatus() {
            return cardStatus;
        }

        public void setCardStatus(List<String> cardStatus) {
            this.cardStatus = cardStatus;
        }

        public List<Long> getGroups() {
            return groups;
        }

        public void setGroups(List<Long> groups) {
            this.groups = groups;
        }

    }
    private ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public DataBinder<CardQuery> getCardQuery(final LocalSettings localSettings) {
        try {
            lock.readLock().lock();
            if (dataBinder == null) {
                // final LocalSettings localSettings = settingsService.getLocalSettings();
                final BeanBinder<CardQuery> binder = BeanBinder.instance(CardQuery.class);
                binder.registerBinder("groups", SimpleCollectionBinder.instance(Group.class, "groups"));
                binder.registerBinder("status", SimpleCollectionBinder.instance(Card.Status.class, "status"));
                binder.registerBinder("expiration", DataBinderHelper.rawPeriodBinder(localSettings, "expiration"));
                binder.registerBinder("member", PropertyBinder.instance(Member.class, "member"));
                binder.registerBinder("number", PropertyBinder.instance(BigInteger.class, "number"));
                binder.registerBinder("cardType", PropertyBinder.instance(CardType.class, "cardType"));
                binder.registerBinder("pageParameters", DataBinderHelper.pageBinder());

                dataBinder = binder;
            }
            return dataBinder;
        } finally {
            lock.readLock().unlock();
        }
    }
    private DataBinder<CardQuery> dataBinder;

    public DataBinder<CardQuery> getDataBinder() {
        if (dataBinder == null) {
            final LocalSettings localSettings = settingsService.getLocalSettings();
            dataBinder = getCardQuery(localSettings);
        }
        return dataBinder;
    }

    public static class SearchCardListResponse extends GenericResponse {

        private List<Card> cards;

        public List<Card> getCards() {
            return cards;
        }

        public void setCards(List<Card> cards) {
            this.cards = cards;
        }

    }

    @RequestMapping(value = "admin/searchCards", method = RequestMethod.POST)
    @ResponseBody
    public SearchCardListResponse submit(@RequestBody CardsParameters params) {
        SearchCardListResponse response = new SearchCardListResponse();
        final LocalSettings localSettings = settingsService.getLocalSettings();
        List<Card.Status> status = new ArrayList();
        for (String s : params.getCardStatus()) {

            status.add(Card.Status.valueOf(s));
        }
        Map<String, Object> queryParameter = new HashMap();
        queryParameter.put("status", status);
        queryParameter.put("member", (Member) elementService.load(params.getMember(), Element.Relationships.USER));
        queryParameter.put("number", params.getNumber());
        queryParameter.put("cardType", cardTypeService.load(params.getCardType()));
        queryParameter.put("groups", params.getGroups());
        Period period = new Period();
        period.setBegin(localSettings.getDateConverter().valueOf(params.getExpiration_begin()));
        period.setEnd(localSettings.getDateConverter().valueOf(params.getExpiration_end()));
        queryParameter.put("expiration", period);
        CardQuery query = getDataBinder().readFromString(queryParameter);
        final List<Card> cards = cardService.search(query);
        response.setCards(cards);
        response.setStatus(0);
        return response;

    }

}
