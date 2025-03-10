package games.pandemic.rules.rules;

import core.AbstractGameState;
import core.rules.Node;
import core.rules.nodetypes.RuleNode;
import games.pandemic.PandemicTurnOrder;
import core.components.Card;
import core.components.Deck;
import core.properties.PropertyString;
import games.pandemic.PandemicGameState;

import static games.pandemic.PandemicGameState.PandemicGamePhase.RPReaction;
import static core.CoreConstants.nameHash;
import static core.CoreConstants.playerHandHash;

@SuppressWarnings("unchecked")
public class ForceRPReaction extends RuleNode {

    public ForceRPReaction() {
        super();
    }

    /**
     * Copy constructor
     * @param forceRPReaction - Node to be copied
     */
    public ForceRPReaction(ForceRPReaction forceRPReaction) {
        super(forceRPReaction);
    }

    @Override
    protected boolean run(AbstractGameState gs) {
        PandemicGameState pgs = (PandemicGameState)gs;
        int nPlayers = gs.getNPlayers();

        for (int i = 0; i < nPlayers; i++) {
            Deck<Card> ph = (Deck<Card>) pgs.getComponent(playerHandHash, i);
            int nCards = ph.getSize();
            for (int cp = 0; cp < nCards; cp++) {
                Card card = ph.getComponents().get(cp);
                if (((PropertyString)card.getProperty(nameHash)).value.equals("Resilient Population")) {
                    ((PandemicTurnOrder)pgs.getTurnOrder()).addReactivePlayer(i);
                    pgs.setGamePhase(RPReaction);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected Node _copy() {
        return new ForceRPReaction(this);
    }
}
