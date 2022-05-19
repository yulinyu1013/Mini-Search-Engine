/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.upenn.cis.stormlite;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import edu.upenn.cis.cis455.storage.CrawlerStorage;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tasks.ITask;

/**
 * Information about the execution of a topology, including
 * the stream routers
 * 
 * @author zives
 *
 */
public class TopologyContext {
	Topology topology;
	CrawlerStorage storage;
	Queue<ITask> taskQueue;
	AtomicLong crawledCounter;
	AtomicLong urlCheckedCounter;
	AtomicLong urlQueuedCounter;
	AtomicLong parsedCounter;
	AtomicLong savedDocCounter;
	AtomicLong inQueueCounter;
	AtomicLong requeueCounter;
	/**
	 * Mappings from stream IDs to routers
	 */
	Map<String,IStreamRouter> next = new HashMap<>();
	
	public TopologyContext(Topology topo, Queue<ITask> theTaskQueue, CrawlerStorage storage) {
		topology = topo;
		taskQueue = theTaskQueue;
		this.storage = storage;
		this.crawledCounter = new AtomicLong(0);
		this.urlCheckedCounter = new AtomicLong(0);
		this.urlQueuedCounter = new AtomicLong(0);
		this.parsedCounter = new AtomicLong(0);
		this.savedDocCounter = new AtomicLong(0);
		this.inQueueCounter = new AtomicLong(0);
		this.requeueCounter = new AtomicLong(0);
	}
	
	public Topology getTopology() {
		return topology;
	}
	
	public void setTopology(Topology topo) {
		this.topology = topo;
	}
	
	public void addStreamTask(ITask next) {
		taskQueue.add(next);
	}
	
	public CrawlerStorage getStorage() {
		return this.storage;
	}
	
	public void incCrawledCounter() {
		this.crawledCounter.incrementAndGet();
	}
	
	public void incUrlCheckedCounter() {
		this.urlCheckedCounter.incrementAndGet();
	}
	public void incUrlQueuedCounter() {
		this.urlQueuedCounter.incrementAndGet();
	}
	public void incSavedDocCounter(long count) {
		this.savedDocCounter.addAndGet(count);
	}
	public void incParsedCounter() {
		this.parsedCounter.incrementAndGet();
	}
	public void incInQueueCounter() {
		this.inQueueCounter.incrementAndGet();
	}
	public void incRequeueCounter() {
		this.requeueCounter.incrementAndGet();
	}
	public Long getRequeueCounter() {
		return this.requeueCounter.get();
	}

	public Long getCrawledCount() {
		return crawledCounter.get();
	}

	public Long getUrlCheckedCount() {
		return urlCheckedCounter.get();
	}

	public Long getUrlQueuedCount() {
		return urlQueuedCounter.get();
	}

	public Long getParsedCount() {
		return parsedCounter.get();
	}

	public Long getSavedDocCount() {
		return savedDocCounter.get();
	}

	public Long getInQueueCount() {
		return inQueueCounter.get();
	}
}
