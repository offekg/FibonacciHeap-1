//written by:
//Timor Eizenman; timore;
//Offek Gil; offekgil;



/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over non-negative integers.
 */
public class FibonacciHeap
{

	private static final double PHI = 1.6180339887; //golden ratio
	private HeapNode min;
	private int size;
	private static int links = 0;
	private static int cuts = 0;
	private int numTrees = 0;
	private int numMarked = 0; 
	
	public FibonacciHeap() {
		min = null;
		size = 0;
	}
	
	public FibonacciHeap(int key) {
		HeapNode min = new HeapNode(key);
		min.setNext(min);
		min.setPrev(min);
		this.min = min;
		size = 1;
		numTrees = 1;
	}
	
	
   /**
    * public boolean empty()
    *
    * precondition: none
    * 
    * The method returns true if and only if the heap
    * is empty.
    *   
    */
    public boolean empty()
    {
    	return (min == null); // returns null if there are no nodes
    }
		
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap. 
    */
    public HeapNode insert(int key)
    {    
    	HeapNode newNode = new HeapNode(key);
    	
    	if(empty()) {
    		min = newNode;
    		min.setNext(min);
    		min.setPrev(min);
    	}
    	else {
	    	listLinkNode(newNode, min); //inserts the new node in the linked list of roots 
	    	if(key < min.getKey()) {
	    		min = newNode;
	    	}
    	}
    	
    	size++;
    	numTrees++;
    	return newNode; 
    }

   /**
    * public void deleteMin()
    *
    * Delete the node containing the minimum key.
    *
    */
    public void deleteMin()
    {
     	if (empty())
     		return;
     	if(size() == 1) {
     		destroyHeap();
     		return;
     	}
     	
     	HeapNode oldMin = findMin();
     	if (oldMin.getRank() > 0) {
     		meldLists(oldMin, oldMin.getChild()); //meld the list of Min's children with the roots list
     		oldMin.setChild(null);
     	}
     	
     	numTrees += oldMin.getRank() - 1;
     	size--;
     	
     	min = min.getNext();
    	skipNode(oldMin);
    	successiveLinking();
    	min = findNewMin();	    
    }
    
    /**
     * private HeapNode findNewMin()
     * finds the current smallest HeapNode in a linked list of nodes
     * 
     * if encounters a root node with a parent, nullifies the parent and falsefies the mark
     */
    private HeapNode findNewMin() {
    	HeapNode current = findMin();
    	HeapNode newMin = current;
    	
    	do {
    		current.parent = null;
    		current.setMarked(false);
    		if (current.getKey() < newMin.getKey()) {
    			newMin = current;
    		}
    		current = current.getNext();
    	}
    	while(current != findMin());
    	
    	return newMin;
    }

   /**
    * public HeapNode findMin()
    *
    * Return the node of the heap whose key is minimal. 
    *
    */
    public HeapNode findMin()
    {
    	return min; 
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Meld the heap with heap2
    *
    */
    public void meld (FibonacciHeap heap2)
    {
    	if (heap2.empty())
    		return; //nothing to meld if heap2 is empty
    	
    	if (empty())
    		min = heap2.findMin();
    	else //both aren't empty
	    	meldLists(findMin(), heap2.findMin());
    	
    	if (findMin().getKey() > heap2.findMin().getKey())
    		min = heap2.findMin();
    	
    	numTrees += heap2.getNumTrees();
    	size += heap2.size();
    	
    	heap2.destroyHeap(); //after the meld, heap2 doesn't exist
    }
    
    /**
     * public static void meldLists(HeapNode a, HeapNode b)
     *
     * Meld the linked list of node b into the linked list of node a
     *puts all of b's list after a
     */
    public static void meldLists(HeapNode a, HeapNode b) {
    	HeapNode aNext = a.getNext();
    	HeapNode bLast = b.getPrev();
    	a.setNext(b);
    	b.setPrev(a);
    	aNext.setPrev(bLast);
    	bLast.setNext(aNext);
    }
    
    public void destroyHeap() {
    	min = null;
    	size = 0;
    }

   /**
    * public int size()
    *
    * Return the number of elements in the heap
    *   
    */
    public int size()
    {
    	return size;
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap. 
    * 
    */
    public int[] countersRep()
    {
		int[] arr = new int[(int) Math.ceil((Math.log(size) / Math.log(PHI)))];
		HeapNode temp = findMin();
		do {
			arr[temp.getRank()] += 1;
			temp = temp.getNext();
		}
		while(temp != findMin());
	    return arr;
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap. 
    *
    */
    public void delete(HeapNode x) 
    {    
    	decreaseKey(x, x.getKey() + 1);
    	deleteMin();
    }

    /**
     * 
     * update links, numTrees
     */
    private void successiveLinking() {
    	HeapNode[] ranks = new HeapNode[((int) Math.ceil((Math.log(size) / Math.log(PHI)))) + 1];
    	HeapNode temp = min;
    	int iterations = numTrees;
    	
    	for (int i = 0; i < iterations; i++) {    		
    		if(ranks[temp.rank] == null) {
    			ranks[temp.rank] = temp;
    			temp = temp.next;
    		}
    		else {
    			HeapNode next = temp.next;
    			
    			do{
    				temp = mergeTrees(ranks[temp.getRank()], temp);
    				ranks[temp.getRank() - 1] = null;
    			}
    			while(ranks[temp.rank] != null);
    			
    			min = temp; //to keep the pointer to the roots linked list
    			ranks[temp.rank] = temp;
    			temp = next;
    		}
    	}
    }
    
   private HeapNode mergeTrees(HeapNode a, HeapNode b) {
	   links++;
	   numTrees--;
	   
	   if(a.getKey() > b.getKey()) { //to make sure a will be the root and b the son
		   HeapNode temp = a;
		   a = b; 
		   b = temp;
	   }
	  
	   skipNode(b);
	   
	   if(a.getRank() == 0) { // a has no children
		   a.setChild(b);
		   b.setNext(b);
		   b.setPrev(b);
	   }
	   else { // a has children
		   listLinkNode(b, a.getChild());
	   }
	   b.setParent(a); //give b, a as a parent pointer
		   
	   a.setRank(a.getRank() + 1);
	   return a; //return the new root 
	}
   
   private void skipNode(HeapNode mid) { //takes HeapNode mid out of it's linked list
	   HeapNode prev = mid.getPrev();
	   HeapNode next = mid.getNext();
	   prev.setNext(next);
	   next.setPrev(prev);
	   mid.setNext(mid);
	   mid.setPrev(mid);
   }
   
   private void listLinkNode(HeapNode node, HeapNode dest) { //inserts node to the relevant linked list of dest (left of dest)
	    node.setPrev(dest.getPrev());
	    node.getPrev().setNext(node);
	    node.setNext(dest);
	    dest.setPrev(node);
   }

/**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * The function decreases the key of the node x by delta. The structure of the heap should be updated
    * to reflect this chage (for example, the cascading cuts procedure should be applied if needed).
    */
    
    public void decreaseKey(HeapNode x, int delta)
    {    
    	HeapNode decreased = x;
    	x.setKey(x.getKey() - delta);
    	
    	if (x.getParent() == null) {  //if x is a root
    		if (x.getKey() < findMin().getKey()) //check if x is now the smallest
    			min = x;
    		return; //no need for further action (no cuts)
    	}
    	
    	if (x.getParent().getKey() <= x.getKey()) {
    		return; //all is fine, no need for cuts and changes
    	}
    	
    	 //if we made x smaller than his parent, we need to cut him
		do { 
			HeapNode parent = x.parent;
			cut(x);
			if (x.isMarked()) {
				numMarked--;
				x.setMarked(false);
			}
			x = parent;
		}
		while(x.isMarked()); //cascading cuts
    	
		if (decreased.getKey() < findMin().getKey())
			min = decreased;
		
		//after we reached a non marked parent / root
		if (x.getParent() == null) //if x is a root
			return; //no need to mark a root
		else { //x is not a root
			x.setMarked(true);
			numMarked++;
		}		
    }

    
    /**
     *  public void cut(HeapNode x)
     *
     * The function cuts x from it's parent, turning it to a root. 
     * enlarges numTrees and numCuts
     * 
     * @pre x is not a root
     */
    public void cut(HeapNode x) {
	    x.getParent().setRank(x.getParent().getRank() - 1);
    	if (x.getParent().getChild() == x) {//validates x's parent's "child" pointer
	    	if (x.getNext() == x)
	    		x.getParent().setChild(null);
	    	else
	    		x.getParent().setChild(x.getNext());
	    }
	    x.setParent(null); //because x will be a root
	    
	    skipNode(x); //takes x out of it's current linked list
	    listLinkNode(x, min); //puts x in the roots linked list
	    
	    numTrees++;
	    cuts++;
    }
    
   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap. 
    */
    public int potential() 
    {    
    	return (numTrees + 2*numMarked); 
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the run-time of the program.
    * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of 
    * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value 
    * in its root.
    */
    public static int totalLinks()
    {    
    	return links;
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the run-time of the program.
    * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods). 
    */
    public static int totalCuts()
    {    
    	return cuts;
    }
    
	public int getNumTrees() {
		return numTrees;
	}
	
    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in 
    * another file 
    *  
    */
    public class HeapNode{

	public int key;
	private int rank; //# children
	private boolean marked = false; //true == marked == 1 child has been cut
	private HeapNode child;
	private HeapNode parent;
	private HeapNode next;
	private HeapNode prev;

  /*	public HeapNode(int key, HeapNode next, HeapNode prev) { //constructor when next and prev are known
	    this.key = key;
	    this.next = next;
	    this.prev = prev;
      }
  	*/
  	public HeapNode(int key) { //general constructor of a node
	    this.key = key;
  	}
  	
  	

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean mark) {
		this.marked = mark;
	}

	public HeapNode getChild() {
		return child;
	}

	public void setChild(HeapNode child) {
		this.child = child;
	}

	public HeapNode getParent() {
		return parent;
	}

	public void setParent(HeapNode parent) {
		this.parent = parent;
	}

	public HeapNode getNext() {
		return next;
	}

	public void setNext(HeapNode next) {
		this.next = next;
	}

	public HeapNode getPrev() {
		return prev;
	}

	public void setPrev(HeapNode prev) {
		this.prev = prev;
	}

	public void setKey(int key) {
		this.key = key;
	}


  	public int getKey() {
	    return this.key;
      }

    }
}
