package report;

import common.CommonUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Bookmarks implements ReportElement {
    protected LinkedHashMap<String, LinkedList<String>> bookmarks = new LinkedHashMap<>();
    protected HashMap<String, String> references = new HashMap<>();

    public String register(String heading) {
        String id = CommonUtils.garbage(heading);
        this.references.put(heading, id);
        return id;
    }

    public boolean isRegistered(String heading) {
        return this.references.get(heading) != null && !"".equals(this.references.get(heading));
    }

    public void bookmark(String heading) {
        this.bookmarks.put(heading, new LinkedList<>());
    }

    public void bookmark(String heading, String child) {
        LinkedList<String> children = this.bookmarks.get(heading);
        if (children == null) {
            this.bookmarks.put(heading, new LinkedList<>());
            this.bookmark(heading, child);
        } else {
            children.add(child);
        }
    }

    public void cleanup() {
            this.bookmarks.forEach((section, children) -> {
            if (!this.isRegistered(section) && children.size() == 0) {
                this.bookmarks.remove(section);
            }
            for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
                String child = children.get(i);
                if (!this.isRegistered(child)) {
                    children.remove(i);
                }
            }
        });
    }

    @Override
    public void publish(StringBuilder out) {
        this.cleanup();
        if (this.bookmarks.size() == 0) {
            return;
        }
        out.append("<fo:bookmark-tree>\n");
        for (Map.Entry<String, LinkedList<String>> entry : this.bookmarks.entrySet()) {
            String section = entry.getKey();
            LinkedList<String> children = entry.getValue();
            out.append("\t<fo:bookmark internal-destination=\"")
                    .append(this.references.get(section))
                    .append("\">\n")
                    .append("\t\t<fo:bookmark-title>")
                    .append(Content.fixText(section))
                    .append("</fo:bookmark-title>\n");
            out.append(children.stream().map(aChildren -> (String) aChildren).map(child -> "\t\t<fo:bookmark internal-destination=\"" + this.references.get(child) + "\">\n" + "\t\t\t<fo:bookmark-title>" + Content.fixText(child) + "</fo:bookmark-title>\n" + "\t</fo:bookmark>\n").collect(Collectors.joining("", "", "\t</fo:bookmark>\n")));
        }
        out.append("</fo:bookmark-tree>\n");
    }
}

