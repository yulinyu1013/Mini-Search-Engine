package DBoperations;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class TermIndex {
    @PrimaryKey
    String reducerId;
    List<TermInfo> infoList;
    
    public TermIndex() {
        
    };
    
    public TermIndex(String reducerId) {
        this.reducerId = reducerId;
        infoList = new ArrayList<>();
    }
    
    public void addItemIntoList(TermInfo t) {
        this.infoList.add(t);
    }

}
