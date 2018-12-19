package beacon.dns;

import c2profile.Profile;
import common.CommonUtils;

import java.util.HashMap;
import java.util.Map;

public class ConversationManager {
    protected Map conversations = new HashMap();
    protected int maxtxt;
    protected long idlemask;

    public ConversationManager(Profile c2profile) {
        this.maxtxt = c2profile.getInt(".dns_max_txt");
        this.idlemask = CommonUtils.ipToLong(c2profile.getString(".dns_idle"));
    }

    public RecvConversation getRecvConversation(String id, String dtype) {
        return (RecvConversation) this.getConversation(id, dtype, RecvConversation.class);
    }

    public SendConversation getSendConversationA(String id, String dtype) {
        return (SendConversation) this.getConversation(id, dtype, SendConversationA.class);
    }

    public SendConversation getSendConversationAAAA(String id, String dtype) {
        return (SendConversation) this.getConversation(id, dtype, SendConversationAAAA.class);
    }

    public SendConversation getSendConversationTXT(String id, String dtype) {
        return (SendConversation) this.getConversation(id, dtype, SendConversationTXT.class);
    }

    public Map getConversations(String id) {
        if (!this.conversations.containsKey(id)) {
            this.conversations.put(id, new Entry());
        }
        Entry e = (Entry) this.conversations.get(id);
        e.last = System.currentTimeMillis();
        return e.convos;
    }

    public Object getConversation(String id, String dtype, Class result) {
        Map convos = this.getConversations(id);
        if (!convos.containsKey(dtype)) {
            if (result == RecvConversation.class) {
                RecvConversation c = new RecvConversation(id, dtype);
                convos.put(dtype, c);
                return c;
            }
            if (result == SendConversationA.class) {
                SendConversationA c = new SendConversationA(id, dtype, this.idlemask);
                convos.put(dtype, c);
                return c;
            }
            if (result == SendConversationAAAA.class) {
                SendConversationAAAA c = new SendConversationAAAA(id, dtype, this.idlemask);
                convos.put(dtype, c);
                return c;
            }
            if (result == SendConversationTXT.class) {
                SendConversationTXT c = new SendConversationTXT(id, dtype, this.idlemask, this.maxtxt);
                convos.put(dtype, c);
                return c;
            }
            return null;
        }
        return convos.get(dtype);
    }

    public void removeConversation(String id, String dtype) {
        if (!this.conversations.containsKey(id)) {
            return;
        }
        Map convos = this.getConversations(id);
        convos.remove(dtype);
        if (convos.size() == 0) {
            this.conversations.remove(id);
        }
    }

    public void purge(String id) {
        if (!this.conversations.containsKey(id)) {
            return;
        }
        Entry temp = (Entry) this.conversations.get(id);
        if (System.currentTimeMillis() - temp.last > 15000L) {
            this.conversations.remove(id);
            CommonUtils.print_error("Purged " + temp.convos.size() + " stalled conversation(s) for " + id);
        } else {
            CommonUtils.print_warn("Protected " + temp.convos.size() + " open conversation(s) for " + id);
        }
    }

    private static class Entry {
        public Map convos = new HashMap();
        public long last = System.currentTimeMillis();

        private Entry() {
        }
    }

}

