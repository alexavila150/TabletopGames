package games.terraformingmars.components;

import core.components.Component;
import core.components.Counter;
import games.terraformingmars.TMGameState;
import games.terraformingmars.TMTypes;
import games.terraformingmars.rules.effects.Bonus;

import java.util.Arrays;
import java.util.Objects;

import static utilities.Utils.ComponentType.BOARD_NODE;

public class TMMapTile extends Component {
    int owner = -1;
    int x, y;
    TMTypes.Tile tilePlaced;

    TMTypes.MapTileType type;
    TMTypes.Resource[] resources;

    public TMMapTile(int x, int y) {
        super(BOARD_NODE, "Tile");
        this.x = x;
        this.y = y;
    }

    protected TMMapTile(int x, int y, int componentID) {
        super(BOARD_NODE, componentID);
        this.x = x;
        this.y = y;
    }

    public void setType(TMTypes.MapTileType type) {
        this.type = type;
    }

    public void setResources(TMTypes.Resource[] resources) {
        this.resources = resources;
    }

    public TMTypes.MapTileType getTileType() {
        return type;
    }

    public TMTypes.Resource[] getResources() {
        return resources;
    }

    public TMTypes.Tile getTilePlaced() {
        return tilePlaced;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getOwner() {
        return owner;
    }

    private void setTilePlaced(TMTypes.Tile which, TMGameState gs) {
        tilePlaced = which;
        int player = gs.getCurrentPlayer();

        // Owner is current player
        if (which != TMTypes.Tile.Ocean) {
            owner = player;
        }

        gs.getTilesPlaced()[player].get(which).increment(1);

        // Current player gets resources
        for (TMTypes.Resource res: resources) {
            gs.getPlayerResources()[player].get(res).increment(1);
            gs.getPlayerResourceIncreaseGen()[player].put(res, true);
        }
    }

    public boolean placeTile(TMTypes.Tile which, TMGameState gs) {
        int player = gs.getCurrentPlayer();
        if (tilePlaced == null) {
            if (which == TMTypes.Tile.Ocean) {
                // If ocean, decrease number of tiles available and increase TR
                Counter oceanTiles = gs.getGlobalParameters().get(TMTypes.GlobalParameter.OceanTiles);
                if (oceanTiles != null) {
                    boolean succeeded = oceanTiles.increment(1);
                    if (succeeded) {
                        gs.getPlayerResources()[player].get(TMTypes.Resource.TR).increment(1);
                        gs.getPlayerResourceIncreaseGen()[player].put(TMTypes.Resource.TR, true);
                        setTilePlaced(which, gs);
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else if (which == TMTypes.Tile.Greenery) {
                // If greenery, increase oxygen and TR
                Counter oxygen = gs.getGlobalParameters().get(TMTypes.GlobalParameter.Oxygen);
                if (oxygen != null) {
                    boolean succeeded = oxygen.increment(1);
                    if (succeeded) {
                        gs.getPlayerResources()[player].get(TMTypes.Resource.TR).increment(1);
                        gs.getPlayerResourceIncreaseGen()[player].put(TMTypes.Resource.TR, true);
                    }
                }
                setTilePlaced(which, gs);
            } else {
                // Just place
                setTilePlaced(which, gs);
            }

            // Params might have increased, check bonuses
            for (Bonus b: gs.getBonuses()) {
                b.checkBonus(gs);
            }

            return true;
        }
        return false;
    }

    public void removeTile() {
        owner = -1;
        tilePlaced = null;
    }

    @Override
    public Component copy() {
        return new TMMapTile(x, y, componentID);
    }

    public static TMMapTile parseMapTile(String s) {
        return parseMapTile(s, -1, -1);
    }

    public static TMMapTile parseMapTile(String s, int x, int y) {
        if (s.equals("0")) return null;

        TMMapTile mt = new TMMapTile(x, y);

        String[] split = s.split("-");

        // First element is tile type
        TMTypes.MapTileType type = null;
        try{
            type = TMTypes.MapTileType.valueOf(split[0]);
        } catch(Exception ignored) {}
        if (type == null) {
            type = TMTypes.MapTileType.City;
            mt.setComponentName(split[0]); // Keep city name
        }
        mt.setType(type);

        // The rest are resources existing here
        int nResources = split.length-1;
        TMTypes.Resource[] resources = new TMTypes.Resource[nResources];
        for (int i = 1; i < split.length; i++) {
            resources[i-1] = TMTypes.Resource.valueOf(split[i]);
        }
        mt.setResources(resources);

        return mt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TMMapTile)) return false;
        if (!super.equals(o)) return false;
        TMMapTile tmMapTile = (TMMapTile) o;
        return owner == tmMapTile.owner && x == tmMapTile.x && y == tmMapTile.y && tilePlaced == tmMapTile.tilePlaced && type == tmMapTile.type && Arrays.equals(resources, tmMapTile.resources);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), owner, x, y, tilePlaced, type);
        result = 31 * result + Arrays.hashCode(resources);
        return result;
    }
}
