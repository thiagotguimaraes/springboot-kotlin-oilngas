import { test, expect, APIRequestContext } from '@playwright/test'
import { createWell, deleteWell, getAllWells, getWellById } from '../../utils/apiUtils'
import { WellEntity } from '../../utils/types'
import { wells } from './data'

test.describe('Get Well API Tests', () => {
	let request: APIRequestContext
	let wellIds: string[] = []
	let createdWells: WellEntity[] = []

	test.beforeAll(async ({ playwright }) => {
		request = await playwright.request.newContext()

		for (const payload of wells) {
			const createdWell = await createWell(request, payload)
			createdWells.push(createdWell)
			wellIds.push(createdWell.id)
		}
	})

	test.afterAll(async () => {
		for (const wellId of wellIds) {
			if (wellId) await deleteWell(request, wellId)
		}

		await request.dispose()
	})

	test('should retrieve a well by ID', async () => {
		const fetchedWell = await getWellById(request, wellIds[0])
		expect(JSON.stringify(fetchedWell)).toBe(JSON.stringify(createdWells[0]))
		expect(fetchedWell).not.toBeNull()
	})

	test('should return 400 for non-existent well', async () => {
		const nonExistentWellId = '00000000-0000-0000-0000-000000000000'
		const response = await getWellById(request, nonExistentWellId)
		expect(response.message).toBe('Well not found')
	})

	test('should return 400 for invalid well uuid as id', async () => {
		const invalidUUIDWellId = 'any-string'
		const response = await getWellById(request, invalidUUIDWellId)
		expect(response.status).toBe(400)
	})

	test('should retrieve all wells', async () => {
		const wells = await getAllWells(request)
		expect(Array.isArray(wells)).toBe(true)
		expect(wells.length).toBeGreaterThan(0)

		const wellExists = wellIds.every((id) => wells.some((well) => well.id === id))
		expect(wellExists).toBe(true)
	})
})
