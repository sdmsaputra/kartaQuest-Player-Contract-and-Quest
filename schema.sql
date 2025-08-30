-- KartaPlayerContract Database Schema
-- Use this for setting up a MySQL database.
-- For SQLite, the plugin will handle table creation automatically.

-- Main table for contracts
CREATE TABLE IF NOT EXISTS player_contracts (
    id TEXT PRIMARY KEY,                            -- UUID of the contract
    owner_uuid TEXT NOT NULL,                       -- UUID of the player who created it
    request_item_json TEXT NOT NULL,                -- JSON representation of the requested ItemStack (type, meta, amount)
    reward_type TEXT NOT NULL CHECK(reward_type IN ('MONEY', 'ITEM')), -- Type of reward
    reward_money REAL,                              -- Amount of money for the reward, if type is MONEY
    reward_items_json TEXT,                         -- JSON of reward items, if type is ITEM
    taken_by_uuid TEXT,                             -- UUID of the player who took the contract
    status TEXT NOT NULL,                           -- OPEN, TAKEN, DELIVERED, COMPLETED, CANCELLED, EXPIRED
    created_at INTEGER NOT NULL,                    -- Unix timestamp of creation
    expires_at INTEGER,                             -- Unix timestamp of expiration, nullable
    delivered_progress_json TEXT                    -- JSON map for tracking partial deliveries
);

-- Table for storing items held in escrow for item-based rewards
CREATE TABLE IF NOT EXISTS escrow_items (
    contract_id TEXT PRIMARY KEY,                   -- Foreign key to player_contracts
    items_json TEXT NOT NULL                        -- Serialized list of ItemStacks
);

-- Table for storing items delivered by a contractor, awaiting acceptance
CREATE TABLE IF NOT EXISTS delivery_packages (
    delivery_id TEXT PRIMARY KEY,                   -- Unique ID for the delivery
    contract_id TEXT NOT NULL,                      -- The contract this delivery is for
    contractor_uuid TEXT NOT NULL,                  -- Who made the delivery
    items_json TEXT NOT NULL,                       -- The items that were delivered
    delivered_at INTEGER NOT NULL                   -- Unix timestamp of delivery
);

-- Table for storing items that need to be returned to a player (e.g. rejected deliveries, cancelled contract rewards)
CREATE TABLE IF NOT EXISTS claim_box_items (
    package_id TEXT PRIMARY KEY,                    -- Unique ID for this package of items
    player_uuid TEXT NOT NULL,                      -- The player who needs to claim these items
    items_json TEXT NOT NULL,                       -- The items to be claimed
    reason TEXT,                                    -- Optional reason (e.g., "Rejected Delivery")
    created_at INTEGER NOT NULL
);

-- Table for tracking player history and statistics
CREATE TABLE IF NOT EXISTS contract_history (
    entry_id INTEGER PRIMARY KEY AUTOINCREMENT,
    contract_id TEXT NOT NULL,
    player_uuid TEXT NOT NULL,
    action TEXT NOT NULL,                           -- e.g., CREATED, TOOK, COMPLETED, CANCELLED
    metadata_json TEXT,                             -- Extra data about the action
    timestamp INTEGER NOT NULL
);
