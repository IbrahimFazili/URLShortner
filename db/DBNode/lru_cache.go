package main

import (
	"container/list"
	"sync"
	"time"
)

type LRUCache struct {
	data        *list.List
	lookupTable map[string]*list.Element
	size        uint32
	lock        sync.Mutex
}

type CacheItem struct {
	data      string
	createdAt int64
}

const CACHE_ITEM_EXPIRY_TIME_S = 300

func MakeLRUCache(size uint32) LRUCache {
	cache := LRUCache{data: list.New(), size: size, lookupTable: make(map[string]*list.Element, size)}

	return cache
}

func (cache *LRUCache) Find(url string) (DBEntry, bool) {
	cache.lock.Lock()
	defer cache.lock.Unlock()

	if element, found := cache.lookupTable[url]; found {
		if time.Now().Unix()-element.Value.(CacheItem).createdAt > CACHE_ITEM_EXPIRY_TIME_S {
			// item expired
			cache.data.Remove(element)
			delete(cache.lookupTable, url)
			return DBEntry{}, false
		}

		cache.data.MoveToBack(element)
		return DBEntry{ShortURL: url, LongURL: element.Value.(CacheItem).data}, true
	}

	return DBEntry{}, false
}

func (cache *LRUCache) Add(entry DBEntry) {
	cache.lock.Lock()
	defer cache.lock.Unlock()

	if cache.data.Len() >= int(cache.size) {
		frontEl := cache.data.Front()
		deleted := cache.data.Remove(frontEl).(CacheItem).data
		delete(cache.lookupTable, deleted)
	}

	newCacheItem := cache.data.PushBack(CacheItem{data: entry.LongURL, createdAt: time.Now().Unix()})
	cache.lookupTable[entry.ShortURL] = newCacheItem
}

func (cache *LRUCache) UpdateValue(entry DBEntry) {
	cache.lock.Lock()
	defer cache.lock.Unlock()

	if element, found := cache.lookupTable[entry.ShortURL]; found {
		element.Value = CacheItem{data: entry.LongURL, createdAt: time.Now().Unix()}
	}
}

func (cache *LRUCache) Delete(url string) {
	cache.lock.Lock()
	defer cache.lock.Unlock()

	if element, found := cache.lookupTable[url]; found {
		cache.data.Remove(element)
		delete(cache.lookupTable, url)
	}
}

func (cache *LRUCache) Purge() {
	cache.data.Init()
	for k := range cache.lookupTable {
		delete(cache.lookupTable, k)
	}
}
