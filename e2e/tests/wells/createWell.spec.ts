import { test, expect, APIRequestContext } from '@playwright/test'
import { createWell, deleteWell, getWellById } from '../../utils/apiUtils'
import { wells } from './data'

test.describe('Create Well API Tests', () => {
	let request: APIRequestContext
	let wellId: string

	test.beforeAll(async ({ playwright }) => {
		request = await playwright.request.newContext()
	})

	test.afterAll(async () => {
		if (wellId) {
			await deleteWell(request, wellId)
		}
		await request.dispose()
	})

	test('should create a well and verify it exists', async () => {
		const well = wells[0]

		// Create a well
		const createdWell = await createWell(request, well)
		wellId = createdWell.id

		// Verify the well exists
		const fetchedWell = await getWellById(request, wellId)
		expect(JSON.stringify(fetchedWell)).toBe(JSON.stringify(createdWell))
		expect(fetchedWell).not.toBeNull()
	})
})
