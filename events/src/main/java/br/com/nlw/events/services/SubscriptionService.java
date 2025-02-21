package br.com.nlw.events.services;

import br.com.nlw.events.dto.SubscriptionRankingByUser;
import br.com.nlw.events.dto.SubscriptionRankingItem;
import br.com.nlw.events.dto.SubscriptionResponse;
import br.com.nlw.events.exceptions.EventNotFoundException;
import br.com.nlw.events.exceptions.SubscriptionConflictException;
import br.com.nlw.events.exceptions.UserIndicatorNotFoundException;
import br.com.nlw.events.model.Event;
import br.com.nlw.events.model.Subscription;
import br.com.nlw.events.model.User;
import br.com.nlw.events.repository.EventRepository;
import br.com.nlw.events.repository.SubscriptionRepository;
import br.com.nlw.events.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class SubscriptionService {
    @Autowired
    private EventRepository evtRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private SubscriptionRepository subRepo;

    public SubscriptionResponse createNewSubscription(String eventName, User user, Integer userId){
        Subscription sub = new Subscription();

        Event evt = evtRepo.findByPrettyName(eventName);
        if(evt == null){
            throw new EventNotFoundException("Evento " + eventName + " não existe.");
        }

        User userRec = userRepo.findByUserEmail(user.getUserEmail());
        if(userRec == null){
            userRec = userRepo.save(user);
        }

        User userIndic = null;
        if(userId != null){
            userIndic = userRepo.findById(userId).orElse(null);
            if(userIndic == null){
                throw new UserIndicatorNotFoundException("Usuário indicador "+userId+" não existe.");
            }
        }

        sub.setEvent(evt);
        sub.setSubscriber(userRec);
        sub.setIndication(userIndic);

        Subscription tmpSub = subRepo.findByEventAndSubscriber(evt, userRec);
        if(tmpSub != null){
            throw new SubscriptionConflictException("Inscrição para o usuário "+ userRec.getUserName() + " no evento "+ evt.getTitle() + " já existe.");
        }
        Subscription res = subRepo.save(sub);
        return new SubscriptionResponse(res.getSubscriptionNumber(), "http://codecraft.com/subscription/"+res.getEvent().getPrettyName()+"/"+res.getSubscriber().getUserId());
    };

    public List<SubscriptionRankingItem> getCompleteRanking(String prettyName){
        Event evt = evtRepo.findByPrettyName(prettyName);
        if(evt == null){
            throw new EventNotFoundException("Ranking do evento "+ prettyName + " não existe.");
        }
        return subRepo.generateRanking(evt.getEventId());
    }
    public SubscriptionRankingByUser getRankingByUser(String prettyName, Integer userId){
        List<SubscriptionRankingItem> ranking =  getCompleteRanking(prettyName);

        SubscriptionRankingItem item = ranking.stream().filter(i-> i.userId().equals(userId)).findFirst().orElse(null);
        if(item == null){
            throw new UserIndicatorNotFoundException("Não há inscrições por indicação do usuário " + userId);
        }
        Integer posicao = IntStream.range(0, ranking.size())
                                    .filter(pos -> ranking.get(pos).userId().equals(userId))
                                    .findFirst().getAsInt();

        return new SubscriptionRankingByUser(item, posicao+1);
    }

}
